package com.careerpath.ml;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe in-memory store for the trained ML model state.
 *
 * <p>Uses an {@link AtomicReference} for lock-free reads and atomic swap
 * on retraining — ensures the serving path never blocks on a training run.
 *
 * <p>The entire trained state is captured in an immutable {@link ModelSnapshot}.
 * A retrain simply replaces the reference; readers using the old snapshot
 * continue safely until GC collects it.
 */
@Component
public class MLModelState {

    /**
     * Immutable snapshot of a fully trained model.
     * Created by {@link MLModelTrainer} and stored atomically here.
     */
    public static final class ModelSnapshot {

        private final KMeansClusterer        clusterer;             // trained K-Means model
        private final Map<Long, FeatureVector> careerVectors;       // careerId → ideal profile
        private final double[][]             clusterCareerAffinity; // [cluster][career index]
        private final List<Long>             careerIdOrder;         // ordered list of career IDs
        private final Map<Integer, String>   clusterLabels;         // cluster index → archetype name

        // Metadata
        private final int           trainingDataSize;
        private final int           kClusters;
        private final int           iterations;
        private final LocalDateTime trainedAt;

        public ModelSnapshot(
                KMeansClusterer clusterer,
                Map<Long, FeatureVector> careerVectors,
                double[][] clusterCareerAffinity,
                List<Long> careerIdOrder,
                Map<Integer, String> clusterLabels,
                int trainingDataSize,
                int kClusters,
                int iterations,
                LocalDateTime trainedAt) {

            this.clusterer             = clusterer;
            this.careerVectors         = careerVectors;
            this.clusterCareerAffinity = clusterCareerAffinity;
            this.careerIdOrder         = careerIdOrder;
            this.clusterLabels         = clusterLabels;
            this.trainingDataSize      = trainingDataSize;
            this.kClusters             = kClusters;
            this.iterations            = iterations;
            this.trainedAt             = trainedAt;
        }

        public KMeansClusterer          getClusterer()             { return clusterer; }
        public Map<Long, FeatureVector> getCareerVectors()         { return careerVectors; }
        public double[][]               getClusterCareerAffinity() { return clusterCareerAffinity; }
        public List<Long>               getCareerIdOrder()         { return careerIdOrder; }
        public Map<Integer, String>     getClusterLabels()         { return clusterLabels; }
        public int                      getTrainingDataSize()      { return trainingDataSize; }
        public int                      getKClusters()             { return kClusters; }
        public int                      getIterations()            { return iterations; }
        public LocalDateTime            getTrainedAt()             { return trainedAt; }
    }

    // Atomic model reference — null until first successful training run
    private final AtomicReference<ModelSnapshot> snapshotRef = new AtomicReference<>();

    /**
     * Atomically replace the current model with a newly trained snapshot.
     * Called by {@link MLModelTrainer} after a successful training run.
     */
    public void update(ModelSnapshot newSnapshot) {
        snapshotRef.set(newSnapshot);
    }

    /**
     * Returns the current trained model snapshot, or {@link Optional#empty()} if
     * training has not yet completed (e.g., insufficient data on first startup).
     */
    public Optional<ModelSnapshot> getSnapshot() {
        return Optional.ofNullable(snapshotRef.get());
    }

    /** Returns {@code true} if the model has been trained at least once. */
    public boolean isTrained() {
        return snapshotRef.get() != null;
    }
}
