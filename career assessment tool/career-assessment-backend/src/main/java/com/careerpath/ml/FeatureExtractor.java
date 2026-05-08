package com.careerpath.ml;

import com.careerpath.model.AssessmentResult;
import com.careerpath.model.Career;
import com.careerpath.model.RecommendationRule;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Converts domain objects into {@link FeatureVector}s for the ML pipeline.
 *
 * <h3>Data preprocessing steps applied here:</h3>
 * <ol>
 *   <li>Category → dimension index lookup via {@link FeatureVector#FEATURE_NAMES}</li>
 *   <li>Score normalization: raw DB values (0–100) → [0.0, 1.0]</li>
 *   <li>Missing category fill: unknown categories default to 0.5 (neutral midpoint)</li>
 *   <li>Career ideal vector: rule weights and minScore thresholds → normalized ideal profile</li>
 * </ol>
 */
@Component
public class FeatureExtractor {

    /**
     * Importance weights per assessment block — used in weighted similarity.
     * Personality is weighted slightly higher as it's the most reliable predictor.
     */
    public static final double[] DIMENSION_WEIGHTS = buildWeights(1.2, 1.0, 1.1);

    // ── Student extraction ────────────────────────────────────────────────────

    /**
     * Extract a student feature vector from their {@link AssessmentResult}.
     *
     * <p>Fills dimensions in order: personality → skills → interest.
     * Dimensions for assessment sections not yet completed default to 0.5 (neutral).
     */
    public FeatureVector extractStudent(AssessmentResult result) {
        double[] v = new double[FeatureVector.DIMENSION];
        Arrays.fill(v, 0.5);  // neutral default for missing/incomplete sections

        mergeScores(v, result.getPersonalityScores());
        mergeScores(v, result.getSkillsScores());
        mergeScores(v, result.getInterestScores());

        return new FeatureVector("student:" + result.getStudent().getId(), v);
    }

    // ── Career ideal profile extraction ───────────────────────────────────────

    /**
     * Build a career's "ideal student profile" vector from its {@link RecommendationRule}s.
     *
     * <p>Algorithm:
     * <ul>
     *   <li>Each rule specifies: category, minScore threshold, and weight.</li>
     *   <li>The ideal score for a dimension = minScore (what the career requires) +
     *       a weight-proportional bonus (the more important the rule, the higher the ideal).</li>
     *   <li>Final per-dimension value = weighted average of all rules touching that dimension.</li>
     *   <li>Dimensions with no rules get a low default (0.3) indicating lower importance.</li>
     * </ul>
     */
    public FeatureVector extractCareer(Career career, List<RecommendationRule> rules) {
        double[] v           = new double[FeatureVector.DIMENSION];
        double[] totalWeight = new double[FeatureVector.DIMENSION];

        for (RecommendationRule rule : rules) {
            int idx = categoryIndex(rule.getCategory());
            if (idx < 0) continue;

            // Ideal score: at least the threshold, boosted by rule importance
            // e.g. minScore=70, weight=2.0 → idealNorm ≈ 0.70 + 0.04 = 0.74
            double idealNorm = Math.min(1.0,
                (rule.getMinScore() / 100.0) + (rule.getWeight() / 10.0) * 0.2);

            v[idx]           += idealNorm * rule.getWeight();
            totalWeight[idx] += rule.getWeight();
        }

        // Normalize each dimension by total rule weight
        for (int i = 0; i < FeatureVector.DIMENSION; i++) {
            if (totalWeight[i] > 0) {
                v[i] = Math.min(1.0, v[i] / totalWeight[i]);
            } else {
                v[i] = 0.3; // dimension not covered by any rule → low requirement
            }
        }

        return new FeatureVector("career:" + career.getId(), v);
    }

    // ── Public utility ────────────────────────────────────────────────────────

    /**
     * Returns the feature dimension index for a category name (case-insensitive),
     * or -1 if the category is not in the canonical feature schema.
     */
    public static int categoryIndex(String category) {
        if (category == null) return -1;
        for (int i = 0; i < FeatureVector.FEATURE_NAMES.length; i++) {
            if (FeatureVector.FEATURE_NAMES[i].equalsIgnoreCase(category.trim())) return i;
        }
        return -1;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Merge a scores map (category → 0–100 raw score) into the target
     * feature vector array, converting to [0.0, 1.0] scale.
     */
    private void mergeScores(double[] target, Map<String, Double> scores) {
        if (scores == null || scores.isEmpty()) return;
        scores.forEach((category, rawScore) -> {
            int idx = categoryIndex(category);
            if (idx >= 0 && rawScore != null) {
                target[idx] = Math.min(1.0, Math.max(0.0, rawScore / 100.0));
            }
        });
    }

    /**
     * Build dimension weights: each block of 5 dimensions gets its own multiplier.
     * @param personalityW  weight for indices 0–4 (Big Five)
     * @param skillsW       weight for indices 5–9 (Skills)
     * @param interestW     weight for indices 10–14 (Interest)
     */
    private static double[] buildWeights(double personalityW, double skillsW, double interestW) {
        double[] w = new double[FeatureVector.DIMENSION];
        for (int i = 0; i < 5;  i++) w[i]      = personalityW;
        for (int i = 5; i < 10; i++) w[i]      = skillsW;
        for (int i = 10; i < 15; i++) w[i]     = interestW;
        return w;
    }
}
