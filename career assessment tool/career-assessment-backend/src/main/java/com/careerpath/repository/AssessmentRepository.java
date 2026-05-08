package com.careerpath.repository;

import com.careerpath.model.Assessment;
import com.careerpath.model.enums.AssessmentStatus;
import com.careerpath.model.enums.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByStudentIdOrderByStartedAtDesc(Long studentId);

    Optional<Assessment> findTopByStudentIdAndTypeOrderByStartedAtDesc(Long studentId, AssessmentType type);

    long countByStatus(AssessmentStatus status);

    long countByStudentIdAndStatus(Long studentId, AssessmentStatus status);
}
