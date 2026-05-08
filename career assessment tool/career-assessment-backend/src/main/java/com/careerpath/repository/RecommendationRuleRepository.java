package com.careerpath.repository;

import com.careerpath.model.RecommendationRule;
import com.careerpath.model.enums.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, Long> {

    List<RecommendationRule> findByActiveTrueOrderByCareerId();

    List<RecommendationRule> findByCareerId(Long careerId);

    List<RecommendationRule> findByCareerIdAndAssessmentType(Long careerId, AssessmentType type);

    default List<RecommendationRule> findAllActive() {
        return findByActiveTrueOrderByCareerId();
    }
}
