package com.careerpath.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * K-Means++ clustering algorithm — pure Java, no external dependencies.
 *
 * <h3>Algorithm overview:</h3>
 * <ol>
 *   <li><b>K-Means++ Initialization</b> — Seeds centroids probabilistically
 *       (weighted by squared distance to nearest existing centroid). This
 *       avoids poor starting configurations and guarantees O(log k) approximation.</li>
 *   <li><b>Assignment step</b> — Each point is assigned to the nearest centroid
 *       using Euclidean distance.</li>
 *   <li><b>Update step</b> — Each centroid is moved to the mean of its assigned
 *       points. Empty clusters are re-seeded from a random data point.</li>
 *   <li><b>Convergence check</b> — Total centroid shift &lt; {@code convergenceThreshold}
 *       OR {@code maxIterations} is reached.</li>
 * </ol>
 *
 * <h3>Inference modes:</h3>
 * <ul>
 *   <li>{@link #predict(FeatureVector)} — hard assignment: index of nearest centroid</li>
 *   <li>{@link #softAssignment(FeatureVector)} — soft assignment: probability distribution
 *       across all clusters via inverse-distance weighting + softmax</li>
 * </ul>
 */
public class KMeansClusterer {

    private final int    k;
    private final int    maxIterations;
    private final double convergenceThreshold;
    private final Random random;

    private double[][] centroids;  // shape: [k][DIMENSION]
    private boolean    trained = false;

    /**
     * @param k                     number of clusters
     * @param maxIterations         hard iteration cap
     * @param convergenceThreshold  stop when total centroid shift &lt; this value
     * @param seed                  random seed for reproducibility
     */
    public KMeansClusterer(int k, int maxIterations, double convergenceThreshold, long seed) {
        this.k                    = k;
        this.maxIterations        = maxIterations;
        this.convergenceThreshold = convergenceThreshold;
        this.random               = new Random(seed);
    }

    // ── Training ──────────────────────────────────────────────────────────────

    /**
     * Fit K-Means++ on a list of feature vectors.
     *
     * @param vectors non-empty list with at least k entries
     * @return number of iterations until convergence
     * @throws IllegalArgumentException if fewer than k vectors are provided
     */
    public int fit(List<FeatureVector> vectors) {
        int n = vectors.size();
        if (n < k) {
            throw new IllegalArgumentException(
                "Need at least k=" + k + " training samples, got " + n);
        }

        int dim = FeatureVector.DIMENSION;
        int[] assignments = new int[n];
        Arrays.fill(assignments, 0);

        // Stage 1: K-Means++ centroid initialization
        centroids = initKMeansPlusPlus(vectors, dim);

        int  iter    = 0;
        boolean changed = true;

        while (changed && iter < maxIterations) {
            changed = false;
            iter++;

            // Assignment step: reassign each point to nearest centroid
            for (int i = 0; i < n; i++) {
                int best = nearestCentroid(vectors.get(i).toArray());
                if (best != assignments[i]) {
                    assignments[i] = best;
                    changed = true;
                }
            }

            // Update step: recompute centroids as cluster means
            double[][] newCentroids = new double[k][dim];
            int[]      counts       = new int[k];

            for (int i = 0; i < n; i++) {
                int c = assignments[i];
                counts[c]++;
                double[] vec = vectors.get(i).toArray();
                for (int d = 0; d < dim; d++) newCentroids[c][d] += vec[d];
            }

            double totalShift = 0.0;
            for (int c = 0; c < k; c++) {
                if (counts[c] == 0) {
                    // Empty cluster: reinitialize to a random data point
                    newCentroids[c] = vectors.get(random.nextInt(n)).toArray();
                } else {
                    for (int d = 0; d < dim; d++) newCentroids[c][d] /= counts[c];
                }
                totalShift += NormalizationUtils.euclidean(centroids[c], newCentroids[c]);
            }

            centroids = newCentroids;
            if (totalShift < convergenceThreshold) break;
        }

        trained = true;
        return iter;
    }

    // ── Inference ─────────────────────────────────────────────────────────────

    /**
     * Hard cluster assignment: returns the index (0-based) of the nearest centroid.
     */
    public int predict(FeatureVector v) {
        requireTrained();
        return nearestCentroid(v.toArray());
    }

    /**
     * Soft cluster assignment: returns a probability distribution over all k clusters.
     *
     * <p>Uses inverse-distance weighting: {@code weight_c = 1 / dist(v, centroid_c)},
     * then applies softmax to convert to probabilities. Points very close to one
     * centroid get near-1.0 probability for that cluster; equidistant points get
     * uniform distribution.
     *
     * @return double[] of length k, values sum to 1.0
     */
    public double[] softAssignment(FeatureVector v) {
        requireTrained();
        double[] arr       = v.toArray();
        double[] invDist   = new double[k];

        for (int c = 0; c < k; c++) {
            double dist = NormalizationUtils.euclidean(arr, centroids[c]);
            invDist[c] = (dist < 1e-10) ? 1e10 : 1.0 / dist;
        }

        return NormalizationUtils.softmax(invDist);
    }

    /**
     * Returns centroid c as a {@link FeatureVector} for comparison/inspection.
     */
    public FeatureVector getCentroid(int clusterIndex) {
        requireTrained();
        if (clusterIndex < 0 || clusterIndex >= k) {
            throw new IndexOutOfBoundsException("Cluster index out of range: " + clusterIndex);
        }
        return new FeatureVector("centroid:" + clusterIndex, centroids[clusterIndex]);
    }

    /**
     * Compute within-cluster sum of squared distances (inertia) — lower is better.
     * Useful for choosing optimal k (elbow method).
     */
    public double computeInertia(List<FeatureVector> vectors) {
        requireTrained();
        double inertia = 0.0;
        for (FeatureVector v : vectors) {
            int    c    = nearestCentroid(v.toArray());
            double dist = NormalizationUtils.euclidean(v.toArray(), centroids[c]);
            inertia += dist * dist;
        }
        return inertia;
    }

    /**
     * Compute cluster sizes: how many vectors from the list belong to each cluster.
     */
    public int[] clusterSizes(List<FeatureVector> vectors) {
        requireTrained();
        int[] sizes = new int[k];
        for (FeatureVector v : vectors) sizes[nearestCentroid(v.toArray())]++;
        return sizes;
    }

    public int     getK()       { return k; }
    public boolean isTrained()  { return trained; }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * K-Means++ seeding: each subsequent centroid is chosen with probability
     * proportional to D(x)² — squared distance to the nearest already-chosen centroid.
     */
    private double[][] initKMeansPlusPlus(List<FeatureVector> vectors, int dim) {
        int n = vectors.size();
        double[][] cents = new double[k][dim];

        // First centroid: uniform random
        cents[0] = vectors.get(random.nextInt(n)).toArray();

        List<double[]> chosen = new ArrayList<>();
        chosen.add(cents[0]);

        for (int c = 1; c < k; c++) {
            // Compute D(x)² for each point = squared distance to nearest chosen centroid
            double[] d2    = new double[n];
            double   total = 0.0;

            for (int i = 0; i < n; i++) {
                double[] pt     = vectors.get(i).toArray();
                double   minD   = Double.MAX_VALUE;
                for (double[] chosen_c : chosen) {
                    double dist = NormalizationUtils.euclidean(pt, chosen_c);
                    if (dist < minD) minD = dist;
                }
                d2[i]  = minD * minD;
                total += d2[i];
            }

            // Weighted random selection
            double threshold = random.nextDouble() * total;
            double cumul     = 0.0;
            int    chosen_i  = n - 1;
            for (int i = 0; i < n; i++) {
                cumul += d2[i];
                if (cumul >= threshold) { chosen_i = i; break; }
            }

            cents[c] = vectors.get(chosen_i).toArray();
            chosen.add(cents[c]);
        }

        return cents;
    }

    private int nearestCentroid(double[] point) {
        double minDist = Double.MAX_VALUE;
        int    best    = 0;
        for (int c = 0; c < k; c++) {
            double dist = NormalizationUtils.euclidean(point, centroids[c]);
            if (dist < minDist) { minDist = dist; best = c; }
        }
        return best;
    }

    private void requireTrained() {
        if (!trained) throw new IllegalStateException("Clusterer not trained. Call fit() first.");
    }
}
