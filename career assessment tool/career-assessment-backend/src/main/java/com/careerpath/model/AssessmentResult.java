package com.careerpath.model;

import com.careerpath.converter.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * One row per student — updated each time they complete an assessment type.
 * Stores aggregated category scores as JSON for all three test types.
 */
@Entity
@Table(name = "assessment_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true, nullable = false)
    private User student;

    // ── Personality scores ──────────────────────────────────────────────────
    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "personality_scores_json", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, Double> personalityScores = new HashMap<>();

    @Column(length = 10)
    private String personalityType;   // e.g. "INTJ"

    @Builder.Default
    @Column(nullable = false)
    private boolean personalityCompleted = false;

    private LocalDateTime personalityCompletedAt;

    // ── Skills scores ───────────────────────────────────────────────────────
    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "skills_scores_json", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, Double> skillsScores = new HashMap<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean skillsCompleted = false;

    private LocalDateTime skillsCompletedAt;

    // ── Interest scores ─────────────────────────────────────────────────────
    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "interest_scores_json", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, Double> interestScores = new HashMap<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean interestCompleted = false;

    private LocalDateTime interestCompletedAt;

    // ── Audit ───────────────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
