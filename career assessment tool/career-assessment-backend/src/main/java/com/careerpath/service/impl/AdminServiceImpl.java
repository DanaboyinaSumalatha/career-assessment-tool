package com.careerpath.service.impl;

import com.careerpath.dto.request.CareerRequest;
import com.careerpath.dto.request.QuestionRequest;
import com.careerpath.dto.response.*;
import com.careerpath.exception.ResourceNotFoundException;
import com.careerpath.model.Career;
import com.careerpath.model.CareerRecommendation;
import com.careerpath.model.Question;
import com.careerpath.model.enums.AssessmentType;
import com.careerpath.model.enums.RoleName;
import com.careerpath.repository.*;
import com.careerpath.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final CareerRepository careerRepository;
    private final AssessmentResultRepository resultRepository;
    private final CareerRecommendationRepository recommendationRepository;
    private final AuthServiceImpl authServiceHelper;
    private final AssessmentServiceImpl assessmentServiceHelper;

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long students     = userRepository.countByRole_Name(RoleName.STUDENT);
        long newStudents  = userRepository.countNewByRoleSince(RoleName.STUDENT,
                LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay());
        long completedAll = resultRepository.findAllCompleted().size();
        long careers      = careerRepository.count();
        long questions    = questionRepository.count();
        long activeCareerPaths = careerRepository.findByStatusOrderByTitleAsc("active").size();

        double persRate   = rate(resultRepository.countByPersonalityCompletedTrue(), students);
        double skillsRate = rate(resultRepository.countBySkillsCompletedTrue(), students);
        double intRate    = rate(resultRepository.countByInterestCompletedTrue(), students);

        return AdminDashboardResponse.builder()
                .totalStudents(students)
                .newStudentsThisMonth(newStudents)
                .completedAssessments(completedAll)
                .totalCareerPaths(careers)
                .totalQuestions(questions)
                .activeCareerPaths(activeCareerPaths)
                .personalityCompletionRate(persRate)
                .skillsCompletionRate(skillsRate)
                .interestCompletionRate(intRate)
                .build();
    }

    // ── Students ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllStudents() {
        return userRepository.findByRole_Name(RoleName.STUDENT)
                .stream()
                .map(user -> {
                    UserResponse ur = authServiceHelper.toUserResponse(user);
                    resultRepository.findByStudentId(user.getId()).ifPresentOrElse(r -> {
                        ur.setPersonalityStatus(r.isPersonalityCompleted() ? "completed" : "pending");
                        ur.setSkillsStatus(r.isSkillsCompleted() ? "completed" : "pending");
                        ur.setInterestStatus(r.isInterestCompleted() ? "completed" : "pending");
                    }, () -> {
                        ur.setPersonalityStatus("pending");
                        ur.setSkillsStatus("pending");
                        ur.setInterestStatus("pending");
                    });
                    return ur;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getStudentById(Long id) {
        return authServiceHelper.toUserResponse(
                userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentResultResponse getStudentResults(Long studentId) {
        com.careerpath.model.AssessmentResult r = resultRepository.findByStudentId(studentId)
                .orElse(com.careerpath.model.AssessmentResult.builder().build());
        AssessmentResultResponse response = assessmentServiceHelper.toResultResponsePublic(r);

        // Attach top career recommendation (rank 1) if available
        List<CareerRecommendation> recs =
                recommendationRepository.findByStudentIdOrderByRankAsc(studentId);
        if (!recs.isEmpty()) {
            response.setTopRecommendation(recs.get(0).getCareer().getTitle());
        }
        return response;
    }

    // ── Questions ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getAllQuestions(String type) {
        List<Question> questions;
        if (type != null && !type.isBlank()) {
            questions = questionRepository.findByTypeAndActiveTrueOrderByOrderIndexAsc(
                    AssessmentType.valueOf(type.toUpperCase()));
        } else {
            questions = questionRepository.findByActiveTrueOrderByTypeAscOrderIndexAsc();
        }
        return questions.stream()
                .map(q -> QuestionResponse.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .type(q.getType().name())
                        .category(q.getCategory())
                        .options(q.getOptions())
                        .orderIndex(q.getOrderIndex())
                        .active(q.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        Question q = Question.builder()
                .text(request.getText())
                .type(request.getType())
                .category(request.getCategory())
                .options(request.getOptions())
                .orderIndex(request.getOrderIndex())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();
        q = questionRepository.save(q);
        return toQResponse(q);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", id));
        q.setText(request.getText());
        q.setType(request.getType());
        q.setCategory(request.getCategory());
        q.setOptions(request.getOptions());
        q.setOrderIndex(request.getOrderIndex());
        q.setActive(Boolean.TRUE.equals(request.getActive()));
        return toQResponse(questionRepository.save(q));
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question", "id", id);
        }
        questionRepository.deleteById(id);
    }

    // ── Career Paths ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CareerResponse> getAllCareerPaths() {
        return careerRepository.findAll().stream()
                .map(this::toCareerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CareerResponse createCareerPath(CareerRequest request) {
        Career career = Career.builder()
                .title(request.getTitle())
                .industry(request.getIndustry())
                .description(request.getDescription())
                .salaryRange(request.getSalaryRange())
                .growthRate(request.getGrowthRate())
                .education(request.getEducation())
                .workStyle(request.getWorkStyle())
                .requiredSkills(request.getRequiredSkills())
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .build();
        return toCareerResponse(careerRepository.save(career));
    }

    @Override
    @Transactional
    public CareerResponse updateCareerPath(Long id, CareerRequest request) {
        Career career = careerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career", "id", id));
        career.setTitle(request.getTitle());
        career.setIndustry(request.getIndustry());
        career.setDescription(request.getDescription());
        career.setSalaryRange(request.getSalaryRange());
        career.setGrowthRate(request.getGrowthRate());
        career.setEducation(request.getEducation());
        career.setWorkStyle(request.getWorkStyle());
        career.setRequiredSkills(request.getRequiredSkills());
        if (request.getStatus() != null) career.setStatus(request.getStatus());
        return toCareerResponse(careerRepository.save(career));
    }

    @Override
    @Transactional
    public void deleteCareerPath(Long id) {
        if (!careerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Career", "id", id);
        }
        careerRepository.deleteById(id);
    }

    // ── Analytics ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        long totalStudents = userRepository.countByRole_Name(RoleName.STUDENT);

        // Monthly registrations (last 6 months)
        List<Map<String, Object>> monthly = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String label = Month.of(month.getMonthValue())
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            long count = userRepository.countNewByRoleSince(RoleName.STUDENT,
                    month.withDayOfMonth(1).toLocalDate().atStartOfDay());
            monthly.add(Map.of("month", label, "students", count));
        }

        // Career distribution from recommendations
        List<Map<String, Object>> careerDist = new ArrayList<>();
        careerRepository.findAll().forEach(career -> {
            long cnt = recommendationRepository.findByStudentIdOrderByRankAsc(0L).size(); // placeholder
            careerDist.add(Map.of("name", career.getTitle(), "value", cnt));
        });

        // Personality type distribution
        List<Map<String, Object>> personalityDist = new ArrayList<>();
        Map<String, Long> typeCounts = new HashMap<>();
        resultRepository.findAll().forEach(r -> {
            if (r.getPersonalityType() != null) {
                typeCounts.merge(r.getPersonalityType(), 1L, Long::sum);
            }
        });
        typeCounts.forEach((type, count) ->
                personalityDist.add(Map.of("type", type, "count", count)));

        // Average skill and interest scores
        List<Map<String, Object>> avgSkills = computeAverageScores(false);
        List<Map<String, Object>> avgInterests = computeAverageScores(true);

        long completed = resultRepository.findAllCompleted().size();
        double overallRate = totalStudents > 0 ? (completed * 100.0 / totalStudents) : 0;

        return AnalyticsResponse.builder()
                .monthlyRegistrations(monthly)
                .careerDistribution(careerDist)
                .personalityTypeDistribution(personalityDist)
                .averageSkillScores(avgSkills)
                .averageInterestScores(avgInterests)
                .totalAssessmentsTaken(completed)
                .overallCompletionRate(overallRate)
                .build();
    }

    private List<Map<String, Object>> computeAverageScores(boolean interest) {
        List<com.careerpath.model.AssessmentResult> results = resultRepository.findAll();
        Map<String, List<Double>> categoryMap = new HashMap<>();
        for (com.careerpath.model.AssessmentResult r : results) {
            Map<String, Double> scores = interest ? r.getInterestScores() : r.getSkillsScores();
            if (scores == null) continue;
            scores.forEach((cat, score) ->
                    categoryMap.computeIfAbsent(cat, k -> new ArrayList<>()).add(score));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        categoryMap.forEach((cat, vals) -> {
            double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            result.add(interest
                    ? Map.of("category", cat, "avgScore", Math.round(avg * 10.0) / 10.0)
                    : Map.of("skill", cat, "avgScore", Math.round(avg * 10.0) / 10.0));
        });
        return result;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private double rate(long count, long total) {
        return total > 0 ? Math.round((count * 100.0 / total) * 10.0) / 10.0 : 0;
    }

    private QuestionResponse toQResponse(Question q) {
        return QuestionResponse.builder()
                .id(q.getId())
                .text(q.getText())
                .type(q.getType().name())
                .category(q.getCategory())
                .options(q.getOptions())
                .orderIndex(q.getOrderIndex())
                .active(q.isActive())
                .build();
    }

    public CareerResponse toCareerResponse(Career c) {
        return CareerResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .industry(c.getIndustry())
                .description(c.getDescription())
                .salaryRange(c.getSalaryRange())
                .growthRate(c.getGrowthRate())
                .education(c.getEducation())
                .workStyle(c.getWorkStyle())
                .requiredSkills(c.getRequiredSkills())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .build();
    }
}
