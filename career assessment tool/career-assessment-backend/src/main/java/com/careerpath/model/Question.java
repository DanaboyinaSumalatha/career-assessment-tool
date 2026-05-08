package com.careerpath.model;

import com.careerpath.model.enums.AssessmentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType type;

    /** Trait/skill/interest category this question measures (e.g. "Extraversion") */
    @Column(nullable = false, length = 100)
    private String category;

    /**
     * Ordered list of answer options (e.g. "Strongly Disagree" … "Strongly Agree").
     * Position index is used to compute the score (0–100).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options",
            joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_value")
    @OrderColumn(name = "option_order")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
