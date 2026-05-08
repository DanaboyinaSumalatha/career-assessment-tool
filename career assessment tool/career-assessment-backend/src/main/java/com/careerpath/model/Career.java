package com.careerpath.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "careers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** e.g. "$60,000 – $120,000" */
    @Column(length = 100)
    private String salaryRange;

    /** e.g. "22% (Much Faster than Average)" */
    @Column(length = 100)
    private String growthRate;

    @Column(length = 255)
    private String education;

    @Column(length = 100)
    private String workStyle;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "career_required_skills",
            joinColumns = @JoinColumn(name = "career_id"))
    @Column(name = "skill")
    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "active";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
