package com.careerpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerResponse {

    private Long id;
    private String title;
    private String industry;
    private String description;
    private String salaryRange;
    private String growthRate;
    private String education;
    private String workStyle;
    private List<String> requiredSkills;
    private String status;
    private String createdAt;
}
