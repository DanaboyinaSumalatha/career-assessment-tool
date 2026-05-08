package com.careerpath.ml;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stateless utility class providing feature normalization and math helpers
 * for the ML pipeline.
 *
 * <h3>Normalization strategies available:</h3>
 * <ul>
 *   <li>{@link #minMaxNormalize} — scales each dimension to [0, 1]; sensitive to outliers</li>
 *   <li>{@link #zScoreNormalize} — standardizes to mean=0, std=1, then clips to [0,1];
 *       more robust when score distributions are skewed</li>
 * </ul>
 */
public final class NormalizationUtils {

    private NormalizationUtils() { /* utility class — no instances */ }

    // ── Normalization ─────────────────────────────────────────────────────────

    /**
     * Min-max normalization: scales each dimension independently to [0.0, 1.0].
     *
     * <p>Formula: {@code x_norm = (x - min) / (max - min)}
     * <p>When {@code max == min} (zero-variance dimension), all values map to 0.5.
     * <p>Returns new {@link FeatureVector} instances — originals are unchanged.
     */
    public static List<FeatureVector> minMaxNormalize(List<FeatureVector> vectors) {
        if (vectors.isEmpty()) return vectors;

        int dim = FeatureVector.DIMENSION;
        double[] min = new double[dim];
        double[] max = new double[dim];
        Arrays.fill(min, Double.MAX_VALUE);
        Arrays.fill(max, -Double.MAX_VALUE);

        for (FeatureVector v : vectors) {
            for (int i = 0; i < dim; i++) {
                if (v.get(i) < min[i]) min[i] = v.get(i);
                if (v.get(i) > max[i]) max[i] = v.get(i);
            }
        }

        return vectors.stream().map(v -> {
            double[] normalized = new double[dim];
            for (int i = 0; i < dim; i++) {
                double range = max[i] - min[i];
                normalized[i] = (range < 1e-10) ? 0.5 : (v.get(i) - min[i]) / range;
            }
            return new FeatureVector(v.getLabel(), normalized);
        }).collect(Collectors.toList());
    }

    /**
     * Z-score standardization: {@code z = (x - mean) / std_dev}.
     *
     * <p>Clips z-scores to [-3, 3], then maps to [0.0, 1.0] via {@code (z + 3) / 6}.
     * Zero-variance dimensions (std ≈ 0) map to 0.5.
     * <p>Returns new {@link FeatureVector} instances — originals are unchanged.
     */
    public static List<FeatureVector> zScoreNormalize(List<FeatureVector> vectors) {
        if (vectors.isEmpty()) return vectors;

        int dim = FeatureVector.DIMENSION;
        int n   = vectors.size();
        double[] mean = new double[dim];
        double[] std  = new double[dim];

        // Compute mean
        for (FeatureVector v : vectors)
            for (int i = 0; i < dim; i++) mean[i] += v.get(i);
        for (int i = 0; i < dim; i++) mean[i] /= n;

        // Compute std
        for (FeatureVector v : vectors)
            for (int i = 0; i < dim; i++) {
                double d = v.get(i) - mean[i];
                std[i] += d * d;
            }
        for (int i = 0; i < dim; i++) std[i] = Math.sqrt(std[i] / n);

        return vectors.stream().map(v -> {
            double[] z = new double[dim];
            for (int i = 0; i < dim; i++) {
                double zi = std[i] < 1e-10 ? 0.0 : (v.get(i) - mean[i]) / std[i];
                // Clip to [-3, 3] then scale to [0, 1]
                z[i] = (Math.max(-3.0, Math.min(3.0, zi)) + 3.0) / 6.0;
            }
            return new FeatureVector(v.getLabel(), z);
        }).collect(Collectors.toList());
    }

    // ── Math utilities ────────────────────────────────────────────────────────

    /**
     * Softmax over an array of raw scores.
     * Produces a probability distribution summing to 1.0.
     * Subtracts max for numerical stability.
     */
    public static double[] softmax(double[] scores) {
        if (scores.length == 0) return scores;
        double max = Arrays.stream(scores).max().orElse(0.0);
        double sum = 0.0;
        double[] exp = new double[scores.length];
        for (int i = 0; i < scores.length; i++) {
            exp[i] = Math.exp(scores[i] - max);
            sum += exp[i];
        }
        for (int i = 0; i < scores.length; i++) exp[i] /= sum;
        return exp;
    }

    /**
     * Euclidean distance between two double arrays of the same length.
     */
    public static double euclidean(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    /**
     * Element-wise dot product of two arrays.
     */
    public static double dot(double[] a, double[] b) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }

    /**
     * L2 norm of an array.
     */
    public static double norm(double[] a) {
        double s = 0.0;
        for (double x : a) s += x * x;
        return Math.sqrt(s);
    }

    /**
     * Clamp a value to [min, max].
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Round to a specified number of decimal places.
     */
    public static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
