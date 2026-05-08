package com.careerpath.ml;

import com.careerpath.model.AssessmentResult;
import com.careerpath.model.Career;
import com.careerpath.model.RecommendationRule;
import com.careerpath.repository.AssessmentResultRepository;
import com.careerpath.repository.CareerRepository;
import com.careerpath.repository.RecommendationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ML Training Pipeline for the career recommendation system.
 *
 * <h3>Seven-stage pipeline:</h3>
 * <pre>
 *  Stage 1 ── Data Collection     Load all fully-completed AssessmentResult rows
 *  Stage 2 ── Feature Extraction  Convert each result to a 15-dim FeatureVector
 *  Stage 3 ── Normalization       Min-max normalize across the training corpus
 *  Stage 4 ── K-Means++           Cluster students into K archetype groups
 *  Stage 5 ── Career Profiles     Build ideal-student vectors per career from rules
 *  Stage 6 ── Affinity Matrix     Pre-compute [cluster × career] cosine similarities
 *  Stage 7 ── Model Commit        Atomically update MLModelState with new snapshot
 * </pre>
 *
 * <p>The trainer auto-runs on {@link ApplicationReadyEvent} (startup) and can be
 * triggered on-demand via the admin API ({@code POST /api/admin/ml/retrain}).
 *
 * <p>Minimum {@value #MIN_TRAINING_SAMPLES} fully-completed student assessments
 * are required before training begins; otherwise training is skipped gracefully.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MLModelTrainer {

    // ── Training hyper-parameters ─────────────────────────────────────────────

    /** Minimum completed student records needed to train a meaningful model. */
    public static final int MIN_TRAINING_SAMPLES = 3;

    /** Number of student archetype clusters. Increase as dataset grows. */
    private static final int K_CLUSTERS = 5;

    /** Hard cap on K-Means iterations per training run. */
    private static final int MAX_ITERATIONS = 150;

    /** K-Means stops early when total centroid shift falls below this. */
    private static final double CONVERGENCE_EPS = 1e-5;

    /** Fixed seed for reproducibility across retraining runs with same data. */
    private static final long RANDOM_SEED = 42L;

    // ── Archetype labels — one per cluster (index must match assignment logic) ─

    private static final String[] ARCHETYPE_NAMES = {
        "Analytical Thinker",      // high: Openness, Analytics, Investigative
        "Creative Communicator",   // high: Creativity, Communication, Artistic
        "Strategic Leader",        // high: Leadership, Conscientiousness, Enterprising
        "Empathetic Helper",       // high: Agreeableness, Social, Communication
        "Structured Executor"      // high: Conscientiousness, Conventional, Analytics
    };

    /**
     * Feature indices that characterize each archetype.
     * Rows correspond to {@link #ARCHETYPE_NAMES}; columns are dimension indices.
     * Archetypes are identified by which centroid has the highest sum of
     * those defining features.
     */
    private static final int[][] ARCHETYPE_FEATURE_INDICES = {
        {0, 7, 10},  // Analytical:  Openness, Analytics, Investigative
        {8, 6, 11},  // Creative:    Creativity, Communication, Artistic
        {9, 1, 13},  // Strategic:   Leadership, Conscientiousness, Enterprising
        {3, 12, 6},  // Empathetic:  Agreeableness, Social, Communication
        {1, 14, 7}   // Structured:  Conscientiousness, Conventional, Analytics
    };

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final AssessmentResultRepository  resultRepository;
    private final RecommendationRuleRepository ruleRepository;
    private final CareerRepository            careerRepository;
    private final FeatureExtractor            featureExtractor;
    private final MLModelState                modelState;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Auto-trains the model at application startup once all beans are ready.
     * Silently skips if insufficient data — this is expected on fresh deployments.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void trainOnStartup() {
        log.info("[ML] Checking for training data at application startup...");
        try {
            TrainingResult result = train();
            if (result.isSuccess()) {
                log.info("[ML] Startup training complete: {} samples → {} clusters, {} iterations",
                    result.getTrainingSamples(), result.getClusters(), result.getIterations());
            } else {
                log.info("[ML] Startup training skipped — {}  (need ≥{} completed assessments)",
                    result.getMessage(), MIN_TRAINING_SAMPLES);
            }
        } catch (Exception e) {
            log.warn("[ML] Startup training encountered an error: {}", e.getMessage());
        }
    }

    /**
     * Execute the full training pipeline. Thread-safe — multiple concurrent calls
     * are safe (last writer wins on the model state).
     *
     * @return {@link TrainingResult} with success/skip metadata
     */
    @Transactional(readOnly = true)
    public TrainingResult train() {

        // ── Stage 1: Data collection ───────────────────────────────────────────
        List<AssessmentResult> corpus = resultRepository.findAllCompleted();
        int n = corpus.size();

        if (n < MIN_TRAINING_SAMPLES) {
            return TrainingResult.skipped(n, MIN_TRAINING_SAMPLES);
        }
        log.info("[ML] Stage 1 — Loaded {} completed student assessment records", n);

        // ── Stage 2: Feature extraction ────────────────────────────────────────
        List<FeatureVector> rawVectors = corpus.stream()
            .map(featureExtractor::extractStudent)
            .collect(Collectors.toList());
        log.info("[ML] Stage 2 — Extracted {} feature vectors (dim={})",
            rawVectors.size(), FeatureVector.DIMENSION);

        // ── Stage 3: Normalization ─────────────────────────────────────────────
        List<FeatureVector> normalizedVectors = NormalizationUtils.minMaxNormalize(rawVectors);
        log.info("[ML] Stage 3 — Applied min-max normalization across training set");

        // ── Stage 4: K-Means++ clustering ─────────────────────────────────────
        int effectiveK = Math.min(K_CLUSTERS, n);
        KMeansClusterer clusterer = new KMeansClusterer(
            effectiveK, MAX_ITERATIONS, CONVERGENCE_EPS, RANDOM_SEED);
        int iterations = clusterer.fit(normalizedVectors);
        int[] clusterSizesArr = clusterer.clusterSizes(normalizedVectors);
        double inertia = clusterer.computeInertia(normalizedVectors);
        log.info("[ML] Stage 4 — K-Means++ converged: k={}, iters={}, inertia={:.4f}",
            effectiveK, iterations, inertia);

        // ── Stage 5: Career ideal profile vectors ─────────────────────────────
        List<Career> careers = careerRepository.findAll();
        List<RecommendationRule> allRules = ruleRepository.findAllActive();
        Map<Long, List<RecommendationRule>> rulesByCareer = allRules.stream()
            .collect(Collectors.groupingBy(r -> r.getCareer().getId()));

        Map<Long, FeatureVector> careerVectors = new LinkedHashMap<>();
        List<Long> careerIdOrder = new ArrayList<>();

        for (Career career : careers) {
            List<RecommendationRule> rules =
                rulesByCareer.getOrDefault(career.getId(), Collections.emptyList());
            FeatureVector careerVec = featureExtractor.extractCareer(career, rules);
            careerVectors.put(career.getId(), careerVec);
            careerIdOrder.add(career.getId());
        }
        log.info("[ML] Stage 5 — Built ideal profile vectors for {} careers", careers.size());

        // ── Stage 6: Cluster-Career affinity matrix ────────────────────────────
        // affinity[c][j] = cosine similarity between cluster c centroid and career j vector
        double[][] affinity = new double[effectiveK][careerIdOrder.size()];
        for (int c = 0; c < effectiveK; c++) {
            FeatureVector centroid = clusterer.getCentroid(c);
            for (int j = 0; j < careerIdOrder.size(); j++) {
                FeatureVector careerVec = careerVectors.get(careerIdOrder.get(j));
                affinity[c][j] = centroid.cosineSimilarity(careerVec);
            }
        }
        log.info("[ML] Stage 6 — Computed {}×{} cluster-career affinity matrix",
            effectiveK, careers.size());

        // ── Stage 7: Model commit ──────────────────────────────────────────────
        Map<Integer, String> clusterLabels = assignArchetypeLabels(clusterer, effectiveK);
        LocalDateTime now = LocalDateTime.now();

        modelState.update(new MLModelState.ModelSnapshot(
            clusterer, careerVectors, affinity, careerIdOrder,
            clusterLabels, n, effectiveK, iterations, now));

        log.info("[ML] Stage 7 — Model committed. Archetypes: {}", clusterLabels);

        // Build cluster size map for the result
        Map<Integer, Integer> clusterSizeMap = new LinkedHashMap<>();
        for (int c = 0; c < effectiveK; c++) clusterSizeMap.put(c, clusterSizesArr[c]);

        return TrainingResult.success(n, effectiveK, iterations, inertia, now,
            clusterSizeMap, clusterLabels);
    }

    // ── Archetype label assignment ─────────────────────────────────────────────

    /**
     * Assign the most fitting archetype name to each cluster centroid.
     *
     * <p>For each archetype, we compute how strongly the centroid exhibits its
     * defining features. The archetype is assigned to the cluster where this
     * signature score is highest. Each archetype is used at most once.
     */
    private Map<Integer, String> assignArchetypeLabels(KMeansClusterer clusterer, int numClusters) {
        Map<Integer, String> labels = new LinkedHashMap<>();
        Set<Integer> usedArchetypes = new HashSet<>();

        for (int c = 0; c < numClusters; c++) {
            FeatureVector centroid  = clusterer.getCentroid(c);
            double        bestScore = -1.0;
            int           bestIdx   = c % ARCHETYPE_NAMES.length;

            for (int a = 0; a < ARCHETYPE_NAMES.length; a++) {
                if (usedArchetypes.contains(a)) continue;
                double score = 0.0;
                for (int fi : ARCHETYPE_FEATURE_INDICES[a]) {
                    if (fi < FeatureVector.DIMENSION) score += centroid.get(fi);
                }
                if (score > bestScore) { bestScore = score; bestIdx = a; }
            }

            usedArchetypes.add(bestIdx);
            labels.put(c, ARCHETYPE_NAMES[bestIdx]);
        }

        return labels;
    }

    // ── Training Result ────────────────────────────────────────────────────────

    /**
     * Describes the outcome of a training run — returned to the admin API caller.
     */
    public static final class TrainingResult {

        private final boolean              success;
        private final String               message;
        private final int                  trainingSamples;
        private final int                  clusters;
        private final int                  iterations;
        private final double               inertia;
        private final LocalDateTime        trainedAt;
        private final Map<Integer, Integer> clusterSizes;
        private final Map<Integer, String> clusterLabels;

        private TrainingResult(boolean success, String message, int trainingSamples,
                               int clusters, int iterations, double inertia,
                               LocalDateTime trainedAt, Map<Integer, Integer> clusterSizes,
                               Map<Integer, String> clusterLabels) {
            this.success         = success;
            this.message         = message;
            this.trainingSamples = trainingSamples;
            this.clusters        = clusters;
            this.iterations      = iterations;
            this.inertia         = inertia;
            this.trainedAt       = trainedAt;
            this.clusterSizes    = clusterSizes;
            this.clusterLabels   = clusterLabels;
        }

        static TrainingResult success(int samples, int k, int iters, double inertia,
                                      LocalDateTime time, Map<Integer, Integer> sizes,
                                      Map<Integer, String> labels) {
            return new TrainingResult(true,
                "Training complete: " + samples + " samples clustered into " + k + " archetypes",
                samples, k, iters, inertia, time, sizes, labels);
        }

        static TrainingResult skipped(int available, int needed) {
            return new TrainingResult(false,
                "Insufficient training data: " + available + "/" + needed + " completed assessments",
                available, 0, 0, 0.0, null, null, null);
        }

        public boolean              isSuccess()         { return success; }
        public String               getMessage()        { return message; }
        public int                  getTrainingSamples(){ return trainingSamples; }
        public int                  getClusters()       { return clusters; }
        public int                  getIterations()     { return iterations; }
        public double               getInertia()        { return inertia; }
        public LocalDateTime        getTrainedAt()      { return trainedAt; }
        public Map<Integer,Integer> getClusterSizes()   { return clusterSizes; }
        public Map<Integer,String>  getClusterLabels()  { return clusterLabels; }
    }
}
