package com.careerpath.ml;

import com.careerpath.model.AssessmentResult;
import com.careerpath.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ML-powered career scoring engine — complements the rule-based engine with
 * three learned signals that are fused into a single ensemble score.
 *
 * <h2>Ensemble scoring formula</h2>
 * <pre>
 *   finalScore = α × ruleScore + β × cosineScore + γ × clusterScore
 *
 *   α = 0.40  (rule-based:        explicit business knowledge, high precision)
 *   β = 0.35  (cosine similarity: dense vector matching, captures nuance)
 *   γ = 0.25  (cluster affinity:  group-level patterns, smooths sparse data)
 * </pre>
 *
 * <h2>Fallback strategy</h2>
 * <p>When the model has not been trained yet (insufficient data), the engine
 * falls back to pure rule-based scoring with no penalty — the admin can
 * trigger {@code POST /api/admin/ml/retrain} once enough data accumulates.
 *
 * <h2>Score definitions</h2>
 * <dl>
 *   <dt>Rule score (0–100)</dt>
 *   <dd>Computed by the legacy {@code RecommendationEngine} — each matching rule
 *       contributes {@code studentScore × weight}; result normalised to 0–100.</dd>
 *
 *   <dt>Cosine similarity score (0–100)</dt>
 *   <dd>Cosine similarity between the student's 15-dim feature vector and the
 *       career's ideal profile vector (built from rule weights + min-score thresholds).
 *       Multiplied by 100 to put it on the same scale as rule scores.</dd>
 *
 *   <dt>Cluster affinity score (0–100)</dt>
 *   <dd>Soft cluster assignment of the student across all K clusters, dot-producted
 *       with the pre-computed cluster–career affinity matrix. This captures
 *       "students like you tend to succeed in this career" signal.</dd>
 * </dl>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MLRecommendationEngine {

    // ── Ensemble weights (must sum to 1.0) ───────────────────────────────────

    /** Rule-based weight — anchors on explicit domain knowledge */
    private static final double ALPHA = 0.40;

    /** Cosine similarity weight — dense vector pattern matching */
    private static final double BETA  = 0.35;

    /** Cluster affinity weight — student-archetype group signal */
    private static final double GAMMA = 0.25;

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final MLModelState   modelState;
    private final FeatureExtractor featureExtractor;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Score all known careers for this student using the three-signal ensemble.
     *
     * @param student             authenticated student entity
     * @param result              their completed assessment result (all 3 tests done)
     * @param ruleScoredCareers   pre-computed rule scores: careerId → 0–100
     * @return list of {@link MLCareerScore}s sorted by {@code finalScore} descending
     */
    public List<MLCareerScore> scoreAll(User student,
                                        AssessmentResult result,
                                        Map<Long, Double> ruleScoredCareers) {

        FeatureVector studentVec = featureExtractor.extractStudent(result);
        Optional<MLModelState.ModelSnapshot> opt = modelState.getSnapshot();

        // ── Fallback: model not trained — use rule-based only ─────────────────
        if (opt.isEmpty()) {
            log.debug("[ML] No trained model available for student {} — rule-based fallback",
                student.getId());
            return ruleScoredCareers.entrySet().stream()
                .map(e -> new MLCareerScore(
                    e.getKey(), e.getValue(), 0.0, 0.0, e.getValue(), "rule-only"))
                .sorted(Comparator.comparingDouble(MLCareerScore::getFinalScore).reversed())
                .collect(Collectors.toList());
        }

        MLModelState.ModelSnapshot model = opt.get();

        // ── Signal 1: Cosine similarity (student ↔ career ideal profile) ──────
        Map<Long, Double> cosineScores = computeCosineScores(studentVec, model);

        // ── Signal 2: Cluster affinity (archetype × career matrix) ────────────
        Map<Long, Double> clusterScores = computeClusterAffinityScores(studentVec, model);

        // ── Determine student's archetype label ───────────────────────────────
        int    clusterIdx  = model.getClusterer().predict(studentVec);
        String archetype   = model.getClusterLabels().getOrDefault(clusterIdx, "Unknown");

        // ── Ensemble: fuse all three signals for each career ──────────────────
        // Iterate over ALL careers that appear in rules OR in the trained model
        Set<Long> allCareerIds = new LinkedHashSet<>(ruleScoredCareers.keySet());
        allCareerIds.addAll(model.getCareerVectors().keySet());

        List<MLCareerScore> scores = new ArrayList<>();

        for (Long careerId : allCareerIds) {
            double ruleScore    = ruleScoredCareers.getOrDefault(careerId, 0.0);
            double cosineScore  = cosineScores.getOrDefault(careerId, 0.0) * 100.0;
            double clusterScore = clusterScores.getOrDefault(careerId, 0.0) * 100.0;

            // If the career has no rules, lean on ML signals more
            boolean hasRules = ruleScoredCareers.containsKey(careerId);
            double finalScore;
            if (hasRules) {
                finalScore = ALPHA * ruleScore + BETA * cosineScore + GAMMA * clusterScore;
            } else {
                // No rules for this career — use ML signals only (redistribute weights)
                finalScore = (BETA / (BETA + GAMMA)) * cosineScore
                           + (GAMMA / (BETA + GAMMA)) * clusterScore;
            }

            finalScore = NormalizationUtils.clamp(
                NormalizationUtils.round(finalScore, 1), 0.0, 100.0);

            scores.add(new MLCareerScore(
                careerId, ruleScore, cosineScore, clusterScore, finalScore, archetype));
        }

        scores.sort(Comparator.comparingDouble(MLCareerScore::getFinalScore).reversed());

        log.debug("[ML] Scored {} careers for student {} | archetype='{}' | top={}pts",
            scores.size(), student.getId(), archetype,
            scores.isEmpty() ? 0 : scores.get(0).getFinalScore());

        return scores;
    }

    /**
     * Convenience method: returns only the student's archetype label without
     * running full scoring. Returns "Unknown" if model is not yet trained.
     */
    public String getArchetypeLabel(AssessmentResult result) {
        Optional<MLModelState.ModelSnapshot> opt = modelState.getSnapshot();
        if (opt.isEmpty()) return "Unknown";
        FeatureVector v = featureExtractor.extractStudent(result);
        int idx = opt.get().getClusterer().predict(v);
        return opt.get().getClusterLabels().getOrDefault(idx, "Unknown");
    }

    // ── Private signal computation ────────────────────────────────────────────

    /**
     * Signal 1 — Cosine similarity between student vector and each career's
     * ideal profile vector. Result ∈ [0.0, 1.0].
     */
    private Map<Long, Double> computeCosineScores(FeatureVector studentVec,
                                                   MLModelState.ModelSnapshot model) {
        Map<Long, Double> scores = new HashMap<>();
        model.getCareerVectors().forEach((careerId, careerVec) ->
            scores.put(careerId,
                studentVec.weightedCosineSimilarity(careerVec, FeatureExtractor.DIMENSION_WEIGHTS)));
        return scores;
    }

    /**
     * Signal 2 — Cluster affinity score for each career.
     *
     * <p>The student gets a soft cluster assignment (probability distribution over
     * K clusters). We then take a weighted sum of each career's affinity scores
     * across all clusters, using the student's cluster probabilities as weights.
     *
     * <pre>
     *   clusterScore[career_j] = Σ_c ( softAssignment[c] × affinity[c][career_j] )
     * </pre>
     */
    private Map<Long, Double> computeClusterAffinityScores(FeatureVector studentVec,
                                                             MLModelState.ModelSnapshot model) {
        double[]   clusterWeights = model.getClusterer().softAssignment(studentVec);
        double[][] affinityMatrix = model.getClusterCareerAffinity();
        List<Long> careerOrder    = model.getCareerIdOrder();

        Map<Long, Double> careerAffinities = new HashMap<>();
        for (int j = 0; j < careerOrder.size(); j++) {
            double affinity = 0.0;
            for (int c = 0; c < clusterWeights.length; c++) {
                if (c < affinityMatrix.length && j < affinityMatrix[c].length) {
                    affinity += clusterWeights[c] * affinityMatrix[c][j];
                }
            }
            careerAffinities.put(careerOrder.get(j), affinity);
        }
        return careerAffinities;
    }

    // ── Result data class ─────────────────────────────────────────────────────

    /**
     * Holds the per-career ensemble score and score breakdown for a single student.
     */
    public static final class MLCareerScore {

        private final Long   careerId;
        private final double ruleScore;     // 0–100, from rule engine
        private final double cosineScore;   // 0–100, from cosine similarity
        private final double clusterScore;  // 0–100, from cluster affinity
        private final double finalScore;    // 0–100, weighted ensemble
        private final String archetypeLabel; // student's nearest cluster name

        public MLCareerScore(Long careerId, double ruleScore, double cosineScore,
                             double clusterScore, double finalScore, String archetypeLabel) {
            this.careerId      = careerId;
            this.ruleScore     = ruleScore;
            this.cosineScore   = cosineScore;
            this.clusterScore  = clusterScore;
            this.finalScore    = finalScore;
            this.archetypeLabel = archetypeLabel;
        }

        public Long   getCareerId()       { return careerId; }
        public double getRuleScore()      { return ruleScore; }
        public double getCosineScore()    { return cosineScore; }
        public double getClusterScore()   { return clusterScore; }
        public double getFinalScore()     { return finalScore; }
        public String getArchetypeLabel() { return archetypeLabel; }

        @Override
        public String toString() {
            return String.format("MLCareerScore{career=%d, rule=%.1f, cosine=%.1f, cluster=%.1f, final=%.1f, archetype='%s'}",
                careerId, ruleScore, cosineScore, clusterScore, finalScore, archetypeLabel);
        }
    }
}
