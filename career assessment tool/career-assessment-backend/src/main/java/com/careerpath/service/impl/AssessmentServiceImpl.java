package com.careerpath.service.impl;

import com.careerpath.dto.request.AssessmentSubmitRequest;
import com.careerpath.dto.response.AssessmentResultResponse;
import com.careerpath.dto.response.CareerRecommendationResponse;
import com.careerpath.dto.response.QuestionResponse;
import com.careerpath.engine.RecommendationEngine;
import com.careerpath.exception.ResourceNotFoundException;
import com.careerpath.model.*;
import com.careerpath.model.enums.AssessmentStatus;
import com.careerpath.model.enums.AssessmentType;
import com.careerpath.repository.*;
import com.careerpath.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentServiceImpl implements AssessmentService {

    private final QuestionRepository questionRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentAnswerRepository answerRepository;
    private final AssessmentResultRepository resultRepository;
    private final CareerRecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final RecommendationEngine recommendationEngine;

    // ── Get Questions ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByType(AssessmentType type) {
        return questionRepository.findByTypeAndActiveTrueOrderByOrderIndexAsc(type)
                .stream()
                .map(this::toQuestionResponse)
                .collect(Collectors.toList());
    }

    // ── Submit Assessment ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AssessmentResultResponse submitAssessment(Long studentId, AssessmentType type,
                                                     AssessmentSubmitRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        // 1. Create or update Assessment record
        Assessment assessment = assessmentRepository
                .findTopByStudentIdAndTypeOrderByStartedAtDesc(studentId, type)
                .filter(a -> a.getStatus() != AssessmentStatus.COMPLETED)
                .orElse(Assessment.builder()
                        .student(student)
                        .type(type)
                        .status(AssessmentStatus.IN_PROGRESS)
                        .build());

        assessment = assessmentRepository.save(assessment);

        // 2. Load all questions of this type (active)
        List<Question> questions = questionRepository.findByTypeAndActiveTrueOrderByOrderIndexAsc(type);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 3. Compute per-category scores
        Map<String, List<Double>> categoryScores = new HashMap<>();

        for (Map.Entry<String, String> entry : request.getAnswers().entrySet()) {
            Long qId;
            try { qId = Long.parseLong(entry.getKey()); }
            catch (NumberFormatException e) { continue; }

            Question question = questionMap.get(qId);
            if (question == null) continue;

            String answerValue = entry.getValue();
            double score = computeScore(question, answerValue);

            AssessmentAnswer answer = AssessmentAnswer.builder()
                    .assessment(assessment)
                    .question(question)
                    .answerValue(answerValue)
                    .scoreValue(score)
                    .build();
            answerRepository.save(answer);

            categoryScores.computeIfAbsent(question.getCategory(), k -> new ArrayList<>()).add(score);
        }

        // 4. Aggregate to avg score per category (0–100)
        Map<String, Double> aggregated = new LinkedHashMap<>();
        categoryScores.forEach((category, scores) -> {
            double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            aggregated.put(category, Math.round(avg * 10.0) / 10.0);
        });

        // 5. Mark assessment completed
        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setCompletedAt(LocalDateTime.now());
        assessmentRepository.save(assessment);

        // 6. Persist / update AssessmentResult row
        AssessmentResult result = resultRepository.findByStudentId(studentId)
                .orElse(AssessmentResult.builder().student(student).build());

        switch (type) {
            case PERSONALITY -> {
                result.setPersonalityScores(aggregated);
                result.setPersonalityType(derivePersonalityType(aggregated));
                result.setPersonalityCompleted(true);
                result.setPersonalityCompletedAt(LocalDateTime.now());
            }
            case SKILLS -> {
                result.setSkillsScores(aggregated);
                result.setSkillsCompleted(true);
                result.setSkillsCompletedAt(LocalDateTime.now());
            }
            case INTEREST -> {
                result.setInterestScores(aggregated);
                result.setInterestCompleted(true);
                result.setInterestCompletedAt(LocalDateTime.now());
            }
        }

        resultRepository.save(result);

        // 7. If all three complete → regenerate recommendations
        if (result.isPersonalityCompleted() && result.isSkillsCompleted() && result.isInterestCompleted()) {
            recommendationEngine.generateRecommendations(student, result);
        }

        log.info("Assessment {} submitted for student {}", type, studentId);
        return toResultResponse(result);
    }

    // ── Get My Results ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AssessmentResultResponse getMyResults(Long studentId) {
        AssessmentResult result = resultRepository.findByStudentId(studentId)
                .orElse(AssessmentResult.builder().build()); // empty — no tests taken yet
        return toResultResponse(result);
    }

    // ── Get Recommendations ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CareerRecommendationResponse> getRecommendations(Long studentId) {
        return recommendationRepository.findByStudentIdOrderByRankAsc(studentId)
                .stream()
                .map(rec -> {
                    Career c = rec.getCareer();
                    return CareerRecommendationResponse.builder()
                            .id(rec.getId())
                            .rank(rec.getRank())
                            .matchScore(rec.getMatchScore())
                            .ruleScore(rec.getRuleScore())
                            .cosineScore(rec.getCosineScore())
                            .clusterScore(rec.getClusterScore())
                            .archetypeLabel(rec.getArchetypeLabel())
                            .title(c.getTitle())
                            .industry(c.getIndustry())
                            .description(c.getDescription())
                            .salaryRange(c.getSalaryRange())
                            .growthRate(c.getGrowthRate())
                            .education(c.getEducation())
                            .workStyle(c.getWorkStyle())
                            .requiredSkills(c.getRequiredSkills())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Converts a text answer to a 0–100 score based on its position in options list.
     * Position 0 → 0,  last position → 100.
     */
    private double computeScore(Question question, String answerValue) {
        List<String> options = question.getOptions();
        if (options == null || options.isEmpty()) return 50.0;

        int index = options.indexOf(answerValue);
        if (index < 0) return 50.0; // unknown value → neutral

        // Linear mapping: option[0] = 0, option[last] = 100
        return (options.size() == 1) ? 100.0
                : (index / (double) (options.size() - 1)) * 100.0;
    }

    /**
     * Simple Big Five → MBTI-like label derived from top traits.
     */
    private String derivePersonalityType(Map<String, Double> scores) {
        StringBuilder type = new StringBuilder();
        type.append(get(scores, "Extraversion") >= 50  ? "E" : "I");
        type.append(get(scores, "Openness") >= 50       ? "N" : "S");
        type.append(get(scores, "Agreeableness") >= 50  ? "F" : "T");
        type.append(get(scores, "Conscientiousness") >= 50 ? "J" : "P");
        return type.toString();
    }

    private double get(Map<String, Double> map, String key) {
        return map.getOrDefault(key, 50.0);
    }

    private AssessmentResultResponse toResultResponse(AssessmentResult r) {
        return AssessmentResultResponse.builder()
                .personality(buildSection(r.isPersonalityCompleted(),
                        r.getPersonalityCompletedAt(),
                        r.getPersonalityScores(),
                        r.getPersonalityType(),
                        true))
                .skills(buildSection(r.isSkillsCompleted(),
                        r.getSkillsCompletedAt(),
                        r.getSkillsScores(),
                        null,
                        false))
                .interest(buildSection(r.isInterestCompleted(),
                        r.getInterestCompletedAt(),
                        r.getInterestScores(),
                        null,
                        false))
                .build();
    }

    private AssessmentResultResponse.AssessmentSection buildSection(
            boolean completed,
            LocalDateTime completedAt,
            Map<String, Double> scores,
            String personalityType,
            boolean isPersonality) {

        String status = completed ? "completed" : "pending";
        if (!completed) {
            return AssessmentResultResponse.AssessmentSection.builder()
                    .status(status)
                    .build();
        }

        List<AssessmentResultResponse.ScoreEntry> entries = new ArrayList<>();
        if (scores != null) {
            scores.forEach((category, score) -> {
                AssessmentResultResponse.ScoreEntry entry = isPersonality
                        ? AssessmentResultResponse.ScoreEntry.builder().trait(category).score(score).build()
                        : AssessmentResultResponse.ScoreEntry.builder().skill(category).score(score).build();
                entries.add(entry);
            });
        }

        return AssessmentResultResponse.AssessmentSection.builder()
                .status(status)
                .completedAt(completedAt != null ? completedAt.toString() : null)
                .scores(entries)
                .type(personalityType)
                .build();
    }

    // Public accessor for AdminServiceImpl
    public AssessmentResultResponse toResultResponsePublic(AssessmentResult r) {
        return toResultResponse(r);
    }

    private QuestionResponse toQuestionResponse(Question q) {
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
}
