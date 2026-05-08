package com.careerpath.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Career recommendation response — matches the shape expected by
 * CareerRecommendationPage.jsx in the frontend.
 *
 * <p>ML fields ({@code ruleScore}, {@code cosineScore}, {@code clusterScore},
 * {@code archetypeLabel}) are included when available, allowing the frontend
 * to render a score breakdown panel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CareerRecommendationResponse {

    private Long         id;
    private Integer      rank;

    // ── Ensemble score ────────────────────────────────────────────────────────
    /** Final ensemble match percentage (0–100). */
    private Double matchScore;

    /** Rule-based component score (0–100). Null when model is in rule-only mode. */
    private Double ruleScore;

    /** Cosine similarity component score (0–100). Null in rule-only mode. */
    private Double cosineScore;

    /** Cluster affinity component score (0–100). Null in rule-only mode. */
    private Double clusterScore;

    /** Student's ML archetype (e.g. "Analytical Thinker"). Null in rule-only mode. */
    private String archetypeLabel;

    // ── Career metadata ───────────────────────────────────────────────────────
    private String       title;
    private String       industry;
    private String       description;
    private String       salaryRange;
    private String       growthRate;
    private String       education;
    private String       workStyle;
    private List<String> requiredSkills;
}
