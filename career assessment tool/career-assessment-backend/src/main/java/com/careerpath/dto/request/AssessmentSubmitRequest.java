package com.careerpath.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class AssessmentSubmitRequest {

    /**
     * Map of { questionId (as String) → answerValue (e.g. "Agree") }
     * Matches the frontend format: { "1": "Agree", "2": "Strongly Disagree", ... }
     */
    @NotEmpty(message = "Answers cannot be empty")
    private Map<String, String> answers;
}
