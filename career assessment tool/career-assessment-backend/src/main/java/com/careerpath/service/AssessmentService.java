package com.careerpath.service;

import com.careerpath.dto.request.AssessmentSubmitRequest;
import com.careerpath.dto.response.AssessmentResultResponse;
import com.careerpath.dto.response.CareerRecommendationResponse;
import com.careerpath.dto.response.QuestionResponse;
import com.careerpath.model.enums.AssessmentType;

import java.util.List;

public interface AssessmentService {

    List<QuestionResponse> getQuestionsByType(AssessmentType type);

    AssessmentResultResponse submitAssessment(Long studentId, AssessmentType type,
                                              AssessmentSubmitRequest request);

    AssessmentResultResponse getMyResults(Long studentId);

    List<CareerRecommendationResponse> getRecommendations(Long studentId);
}
