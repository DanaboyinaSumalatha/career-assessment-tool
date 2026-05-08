package com.careerpath.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assessment_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** The raw answer text chosen by the student (e.g. "Agree") */
    @Column(nullable = false)
    private String answerValue;

    /** Computed score 0–100 based on option position */
    @Column(nullable = false)
    @Builder.Default
    private Double scoreValue = 0.0;
}
