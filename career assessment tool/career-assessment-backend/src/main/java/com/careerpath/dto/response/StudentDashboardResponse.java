package com.careerpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stats block displayed on the student dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {

    private String studentName;
    private boolean personalityCompleted;
    private boolean skillsCompleted;
    private boolean interestCompleted;
    private int completedAssessments;           // 0–3
    private int totalAssessments;               // always 3
    private String personalityType;             // e.g. "INTJ"
    private String topCareerMatch;              // title of #1 recommendation
    private Double topCareerMatchScore;
    private int progressPercent;                // 0–100
}
