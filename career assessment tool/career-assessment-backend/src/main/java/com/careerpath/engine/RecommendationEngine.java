package com.careerpath.engine;

import com.careerpath.ml.MLRecommendationEngine;
import com.careerpath.model.*;
import com.careerpath.repository.CareerRecommendationRepository;
import com.careerpath.repository.CareerRepository;
import com.careerpath.repository.RecommendationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Ensemble career recommendation engine.
 *
 * <h3>Scoring pipeline</h3>
 * <ol>
 *   <li><b>Rule scoring</b> — For each career, iterate active rules. When
 *       {@code studentScore >= rule.minScore}, accumulate {@code studentScore × weight}.
 *       Normalise result to 0–100 by dividing by maximum possible weight sum.</li>
 *   <li><b>ML ensemble</b> — Pass rule scores to {@link MLRecommendationEngine} which
 *       blends them (α=0.40) with cosine similarity (β=0.35) and cluster affinity
 *       (γ=0.25) to produce a final ensemble score per career.</li>
 *   <li><b>Persist top-N</b> — Save top {@value #TOP_N} recommendations with full
 *       score breakdown (rule, cosine, cluster, final) stored on the entity.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationEngine {

    private static final int TOP_N = 5;

    private final RecommendationRuleRepository   ruleRepository;
    private final CareerRepository               careerRepository;
    private final CareerRecommendationRepository recommendationRepository;
    private final MLRecommendationEngine         mlRecommendationEngine;

    @Transactional
    public void generateRecommendations(User student, AssessmentResult result) {

        // ── Stage 1: Flatten all student scores into one map ──────────────────
        Map<String, Double> allScores = new HashMap<>();
        if (result.getPersonalityScores() != null) allScores.putAll(result.getPersonalityScores());
        if (result.getSkillsScores()      != null) allScores.putAll(result.getSkillsScores());
        if (result.getInterestScores()    != null) allScores.putAll(result.getInterestScores());

        // ── Stage 2: Rule-based scoring per career ────────────────────────────
        List<RecommendationRule> allRules = ruleRepository.findAllActive();
        Map<Long, List<RecommendationRule>> rulesByCareer = allRules.stream()
            .collect(Collectors.groupingBy(r -> r.getCareer().getId()));

        // careerId → 0–100 rule score
        Map<Long, Double>  ruleScores = new LinkedHashMap<>();
        // careerId → Career entity (for persistence)
        Map<Long, Career>  careerById = new HashMap<>();

        for (Map.Entry<Long, List<RecommendationRule>> entry : rulesByCareer.entrySet()) {
            Long careerId                = entry.getKey();
            List<RecommendationRule> rules = entry.getValue();

            double maxPossible = rules.stream()
                .mapToDouble(RecommendationRule::getWeight).sum() * 100.0;
            if (maxPossible == 0) continue;

            double earned = 0.0;
            for (RecommendationRule rule : rules) {
                double studentScore = allScores.getOrDefault(rule.getCategory(), 0.0);
                if (studentScore >= rule.getMinScore()) {
                    earned += studentScore * rule.getWeight();
                }
            }

            double ruleScore = Math.min(100.0,
                Math.round((earned / maxPossible * 100.0) * 10.0) / 10.0);

            ruleScores.put(careerId, ruleScore);
            careerById.put(careerId, rules.get(0).getCareer());
        }

        // ── Stage 3: ML ensemble scoring (cosine + cluster + rules) ──────────
        List<MLRecommendationEngine.MLCareerScore> mlScores =
            mlRecommendationEngine.scoreAll(student, result, ruleScores);

        // Ensure all careers referenced in ML scores have entity references
        for (MLRecommendationEngine.MLCareerScore mls : mlScores) {
            careerById.computeIfAbsent(mls.getCareerId(),
                id -> careerRepository.findById(id).orElse(null));
        }

        // ── Stage 4: Take top-N and persist ───────────────────────────────────
        List<MLRecommendationEngine.MLCareerScore> topN = mlScores.stream()
            .filter(mls -> careerById.get(mls.getCareerId()) != null)
            .limit(TOP_N)
            .collect(Collectors.toList());

        recommendationRepository.deleteByStudentId(student.getId());

        for (int i = 0; i < topN.size(); i++) {
            MLRecommendationEngine.MLCareerScore mls = topN.get(i);
            Career career = careerById.get(mls.getCareerId());

            CareerRecommendation rec = CareerRecommendation.builder()
                .student(student)
                .career(career)
                .matchScore(mls.getFinalScore())
                .ruleScore(mls.getRuleScore())
                .cosineScore(mls.getCosineScore())
                .clusterScore(mls.getClusterScore())
                .archetypeLabel(mls.getArchetypeLabel())
                .rank(i + 1)
                .build();
            recommendationRepository.save(rec);
        }

        boolean mlActive = mlRecommendationEngine.getArchetypeLabel(result) != null
            && !mlRecommendationEngine.getArchetypeLabel(result).equals("Unknown");

        log.info("[Engine] {} recommendations persisted for student {} | method={} | top={:.1f}pts",
            topN.size(), student.getId(),
            mlActive ? "ML-ensemble" : "rule-only",
            topN.isEmpty() ? 0.0 : topN.get(0).getFinalScore());
    }
}
