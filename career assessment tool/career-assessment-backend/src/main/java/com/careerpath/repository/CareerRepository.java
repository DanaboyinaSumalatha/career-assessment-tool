package com.careerpath.repository;

import com.careerpath.model.Career;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRepository extends JpaRepository<Career, Long> {

    List<Career> findByStatusOrderByTitleAsc(String status);

    List<Career> findByIndustryIgnoreCase(String industry);

    boolean existsByTitleIgnoreCase(String title);
}
