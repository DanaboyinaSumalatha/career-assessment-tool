package com.careerpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Analytics data sent to the AnalyticsDashboard admin page.
 * All chart data is pre-formatted for Recharts consumption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    /** Monthly registrations: [{month:"Jan", students:12}, ...] */
    private List<Map<String, Object>> monthlyRegistrations;

    /** Career distribution: [{name:"Software Engineer", value:34}, ...] */
    private List<Map<String, Object>> careerDistribution;

    /** Personality type distribution: [{type:"INTJ", count:8}, ...] */
    private List<Map<String, Object>> personalityTypeDistribution;

    /** Average skill scores: [{skill:"Programming", avgScore:72.5}, ...] */
    private List<Map<String, Object>> averageSkillScores;

    /** Average interest scores: [{category:"Investigative", avgScore:65.0}, ...] */
    private List<Map<String, Object>> averageInterestScores;

    /** Summary numbers */
    private long totalAssessmentsTaken;
    private double overallCompletionRate;
}
