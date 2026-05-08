package com.careerpath.repository;

import com.careerpath.model.Question;
import com.careerpath.model.enums.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTypeAndActiveTrueOrderByOrderIndexAsc(AssessmentType type);

    List<Question> findByActiveTrueOrderByTypeAscOrderIndexAsc();

    long countByType(AssessmentType type);

    long countByTypeAndActiveTrue(AssessmentType type);
}
