package com.careerpath.ml;

import java.util.Arrays;

/**
 * Immutable 15-dimensional numeric feature vector representing a student's
 * assessment profile (or a career's ideal student profile).
 *
 * <h3>Dimension layout (index → category):</h3>
 * <pre>
 *  0  Openness             — Big Five personality
 *  1  Conscientiousness    — Big Five personality
 *  2  Extraversion         — Big Five personality
 *  3  Agreeableness        — Big Five personality
 *  4  Neuroticism          — Big Five personality
 *  5  Programming          — Skills assessment
 *  6  Communication        — Skills assessment
 *  7  Analytics            — Skills assessment
 *  8  Creativity           — Skills assessment
 *  9  Leadership           — Skills assessment
 * 10  Investigative        — Interest survey (RIASEC-inspired)
 * 11  Artistic             — Interest survey
 * 12  Social               — Interest survey
 * 13  Enterprising         — Interest survey
 * 14  Conventional         — Interest survey
 * </pre>
 *
 * All values are in [0.0, 1.0] after normalization.
 * Raw DB scores (0–100) are divided by 100 at extraction time.
 */
public final class FeatureVector {

    /** Canonical ordered feature names — index matches dimension slot. */
    public static final String[] FEATURE_NAMES = {
        // Personality — Big Five (indices 0–4)
        "Openness", "Conscientiousness", "Extraversion", "Agreeableness", "Neuroticism",
        // Skills (indices 5–9)
        "Programming", "Communication", "Analytics", "Creativity", "Leadership",
        // Interest — RIASEC-inspired (indices 10–14)
        "Investigative", "Artistic", "Social", "Enterprising", "Conventional"
    };

    public static final int DIMENSION = FEATURE_NAMES.length; // 15

    private final String label;     // "student:<id>" or "career:<id>" or "centroid:<k>"
    private final double[] values;  // length == DIMENSION, values ∈ [0.0, 1.0]

    public FeatureVector(String label, double[] values) {
        if (values.length != DIMENSION) {
            throw new IllegalArgumentException(
                "Expected " + DIMENSION + " features, got " + values.length);
        }
        this.label  = label;
        this.values = Arrays.copyOf(values, DIMENSION);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String   getLabel()  { return label; }
    public double   get(int i)  { return values[i]; }
    public int      dimension() { return DIMENSION; }

    /** Returns a defensive copy of the internal values array. */
    public double[] toArray()   { return Arrays.copyOf(values, DIMENSION); }

    // ── Distance & similarity metrics ─────────────────────────────────────────

    /**
     * Euclidean (L2) distance between two feature vectors.
     * Returns 0.0 when vectors are identical, √DIMENSION when maximally distant.
     */
    public double euclideanDistance(FeatureVector other) {
        double sum = 0.0;
        for (int i = 0; i < DIMENSION; i++) {
            double diff = this.values[i] - other.values[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * Cosine similarity ∈ [0.0, 1.0] (both vectors are non-negative because
     * scores are always in [0, 1]).
     * Returns 0.0 if either vector is a zero vector.
     */
    public double cosineSimilarity(FeatureVector other) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < DIMENSION; i++) {
            dot   += this.values[i] * other.values[i];
            normA += this.values[i] * this.values[i];
            normB += other.values[i] * other.values[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom < 1e-10 ? 0.0 : dot / denom;
    }

    /** L2 norm (magnitude) of this vector. */
    public double norm() {
        double sum = 0.0;
        for (double v : values) sum += v * v;
        return Math.sqrt(sum);
    }

    /**
     * Weighted cosine similarity — each dimension has an importance weight.
     * Useful for emphasizing personality or skills features over others.
     */
    public double weightedCosineSimilarity(FeatureVector other, double[] weights) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < DIMENSION; i++) {
            double w = (weights != null && i < weights.length) ? weights[i] : 1.0;
            dot   += w * this.values[i] * other.values[i];
            normA += w * this.values[i] * this.values[i];
            normB += w * other.values[i] * other.values[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom < 1e-10 ? 0.0 : dot / denom;
    }

    @Override
    public String toString() {
        return String.format("FeatureVector{label='%s', norm=%.3f}", label, norm());
    }
}
