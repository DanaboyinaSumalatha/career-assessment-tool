package com.careerpath.dto.request;

import com.careerpath.model.enums.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String text;

    @NotNull(message = "Assessment type is required")
    private AssessmentType type;

    @NotBlank(message = "Category is required")
    private String category;

    private List<String> options = new ArrayList<>();

    private Integer orderIndex = 0;

    private Boolean active = true;
}
