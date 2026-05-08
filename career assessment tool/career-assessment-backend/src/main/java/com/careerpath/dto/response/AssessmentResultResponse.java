package com.careerpath.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Unified result response sent to the frontend for all three assessment types.
 * Matches the shape expected by TestResultPage.jsx.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentResultResponse {

    private AssessmentSection personality;
    private AssessmentSection skills;
    private AssessmentSection interest;
    private String topRecommendation;   // top career title (rank 1) if available

    // ── Inner types ──────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AssessmentSection {
        private String status;          // "completed" | "pending"
        private String completedAt;
        private List<ScoreEntry> scores;
        private String type;            // personality type e.g. "INTJ" (only for personality)
        private String summary;
    }

    /**
     * A generic score entry.
     * For personality → populate {@code trait}.
     * For skills and interests → populate {@code skill}.
     * The frontend reads item.trait || item.skill.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ScoreEntry {
        private String trait;  // personality category label
        private String skill;  // skills / interest category label
        private Double score;
    }
}
