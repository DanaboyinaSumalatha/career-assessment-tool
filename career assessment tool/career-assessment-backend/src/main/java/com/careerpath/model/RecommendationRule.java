package com.careerpath.model;

import com.careerpath.model.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

/**
 * A rule that links a Career to a specific assessment category with a
 * minimum threshold score and a weight. The recommendation engine sums
 * {@code score × weight} for every rule whose threshold is satisfied.
 */
@Entity
@Table(name = "recommendation_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType assessmentType;

    /** Matches Question.category and the key in the scores map */
    @Column(nullable = false, length = 100)
    private String category;

    /** Minimum score (0–100) the student must achieve to gain points */
    @Column(nullable = false)
    @Builder.Default
    private Double minScore = 50.0;

    /** Contribution weight to the career's composite match score */
    @Column(nullable = false)
    @Builder.Default
    private Double weight = 1.0;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
