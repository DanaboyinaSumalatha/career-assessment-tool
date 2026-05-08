package com.careerpath.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CareerRequest {

    @NotBlank(message = "Career title is required")
    private String title;

    private String industry;
    private String description;
    private String salaryRange;
    private String growthRate;
    private String education;
    private String workStyle;
    private List<String> requiredSkills = new ArrayList<>();
    private String status = "active";
}
