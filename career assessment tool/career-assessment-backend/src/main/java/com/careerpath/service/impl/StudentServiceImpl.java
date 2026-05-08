package com.careerpath.service.impl;

import com.careerpath.dto.request.UpdateProfileRequest;
import com.careerpath.dto.response.CareerRecommendationResponse;
import com.careerpath.dto.response.StudentDashboardResponse;
import com.careerpath.dto.response.UserResponse;
import com.careerpath.exception.ResourceNotFoundException;
import com.careerpath.model.AssessmentResult;
import com.careerpath.model.User;
import com.careerpath.repository.AssessmentResultRepository;
import com.careerpath.repository.CareerRecommendationRepository;
import com.careerpath.repository.UserRepository;
import com.careerpath.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final AssessmentResultRepository resultRepository;
    private final CareerRecommendationRepository recommendationRepository;
    private final AuthServiceImpl authServiceHelper;

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardResponse getDashboard(Long studentId) {
        User student = getStudent(studentId);
        Optional<AssessmentResult> resultOpt = resultRepository.findByStudentId(studentId);

        boolean personalityDone = resultOpt.map(AssessmentResult::isPersonalityCompleted).orElse(false);
        boolean skillsDone      = resultOpt.map(AssessmentResult::isSkillsCompleted).orElse(false);
        boolean interestDone    = resultOpt.map(AssessmentResult::isInterestCompleted).orElse(false);
        int completed = (personalityDone ? 1 : 0) + (skillsDone ? 1 : 0) + (interestDone ? 1 : 0);

        String personalityType = resultOpt.map(AssessmentResult::getPersonalityType).orElse(null);

        // Top career recommendation
        String topCareer = null;
        Double topScore  = null;
        List<com.careerpath.model.CareerRecommendation> recs =
                recommendationRepository.findByStudentIdOrderByRankAsc(studentId);
        if (!recs.isEmpty()) {
            topCareer = recs.get(0).getCareer().getTitle();
            topScore  = recs.get(0).getMatchScore();
        }

        return StudentDashboardResponse.builder()
                .studentName(student.getFirstName() + " " + student.getLastName())
                .personalityCompleted(personalityDone)
                .skillsCompleted(skillsDone)
                .interestCompleted(interestDone)
                .completedAssessments(completed)
                .totalAssessments(3)
                .personalityType(personalityType)
                .topCareerMatch(topCareer)
                .topCareerMatchScore(topScore)
                .progressPercent(completed * 33)
                .build();
    }

    // ── Profile ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long studentId) {
        return authServiceHelper.toUserResponse(getStudent(studentId));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long studentId, UpdateProfileRequest request) {
        User student = getStudent(studentId);

        if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) student.setLastName(request.getLastName());
        if (request.getPhone()     != null) student.setPhone(request.getPhone());
        if (request.getGrade()     != null) student.setGrade(request.getGrade());
        if (request.getBio()       != null) student.setBio(request.getBio());
        if (request.getCity()      != null) student.setCity(request.getCity());

        student = userRepository.save(student);
        return authServiceHelper.toUserResponse(student);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private User getStudent(Long studentId) {
        return userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));
    }
}
