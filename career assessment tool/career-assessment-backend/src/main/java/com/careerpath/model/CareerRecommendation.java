package com.careerpath.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "career_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    /** Final ensemble match percentage 0–100 */
    @Column(nullable = false)
    @Builder.Default
    private Double matchScore = 0.0;

    /** Rule-based component score (0–100) */
    @Column
    @Builder.Default
    private Double ruleScore = 0.0;

    /** Cosine similarity component score (0–100) */
    @Column
    @Builder.Default
    private Double cosineScore = 0.0;

    /** Cluster affinity component score (0–100) */
    @Column
    @Builder.Default
    private Double clusterScore = 0.0;

    /** Student's ML archetype label at time of recommendation (e.g. "Analytical Thinker") */
    @Column(length = 100)
    private String archetypeLabel;

    /** Rank (1 = best match) — column name quoted because RANK is reserved in MySQL 8 */
    @Column(name = "`rank`", nullable = false)
    @Builder.Default
    private Integer rank = 1;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
