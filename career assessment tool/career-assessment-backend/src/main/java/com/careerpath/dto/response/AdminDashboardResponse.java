package com.careerpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * High-level stats shown on the Admin Dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalStudents;
    private long newStudentsThisMonth;
    private long completedAssessments;
    private long totalCareerPaths;
    private long totalQuestions;

    // Completion rates (0–100)
    private double personalityCompletionRate;
    private double skillsCompletionRate;
    private double interestCompletionRate;

    // Quick counts
    private long activeCareerPaths;
}
