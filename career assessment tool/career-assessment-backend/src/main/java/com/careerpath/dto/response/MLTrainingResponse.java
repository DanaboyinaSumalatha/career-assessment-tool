package com.careerpath.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for {@code POST /api/admin/ml/retrain}.
 * Describes the outcome of a model training run, including cluster diagnostics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MLTrainingResponse {

    /** Whether training succeeded ({@code true}) or was skipped ({@code false}). */
    private boolean success;

    /** Human-readable summary of the training outcome. */
    private String message;

    /** Number of student assessment records used to train the model. */
    private int trainingSamples;

    /**
     * Number of K-Means clusters (student archetypes) produced.
     * 0 if training was skipped.
     */
    private int clusters;

    /**
     * Number of K-Means iterations until convergence.
     * 0 if training was skipped.
     */
    private int iterations;

    /**
     * Within-cluster sum of squared distances (inertia).
     * Lower → tighter, more cohesive clusters.
     * Null if training was skipped.
     */
    private Double inertia;

    /**
     * Timestamp when this training run completed.
     * Null if training was skipped.
     */
    private LocalDateTime trainedAt;

    /**
     * Number of students assigned to each cluster.
     * Key = cluster index (0-based), Value = student count.
     * Null if training was skipped.
     *
     * <p>Example: {@code { "0": 12, "1": 8, "2": 9, "3": 10, "4": 6 }}
     */
    private Map<Integer, Integer> clusterSizes;

    /**
     * Human-readable archetype label for each cluster.
     * Key = cluster index (0-based), Value = archetype name.
     * Null if training was skipped.
     *
     * <p>Example: {@code { "0": "Analytical Thinker", "1": "Creative Communicator", ... }}
     */
    private Map<Integer, String> clusterLabels;

    /** Whether the ML model is now active and will be used for recommendations. */
    private boolean modelActive;
}
