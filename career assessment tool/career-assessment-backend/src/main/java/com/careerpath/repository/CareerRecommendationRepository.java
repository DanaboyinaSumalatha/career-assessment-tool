package com.careerpath.repository;

import com.careerpath.model.CareerRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRecommendationRepository extends JpaRepository<CareerRecommendation, Long> {

    List<CareerRecommendation> findByStudentIdOrderByRankAsc(Long studentId);

    void deleteByStudentId(Long studentId);
}
