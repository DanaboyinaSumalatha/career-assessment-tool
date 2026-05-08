package com.careerpath.repository;

import com.careerpath.model.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {

    Optional<AssessmentResult> findByStudentId(Long studentId);

    @Query("SELECT ar FROM AssessmentResult ar WHERE ar.personalityCompleted = true AND ar.skillsCompleted = true AND ar.interestCompleted = true")
    List<AssessmentResult> findAllCompleted();

    long countByPersonalityCompletedTrue();

    long countBySkillsCompletedTrue();

    long countByInterestCompletedTrue();
}
