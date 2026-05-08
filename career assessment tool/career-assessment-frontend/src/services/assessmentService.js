/**
 * assessmentService.js
 * Axios calls for all assessment-related endpoints.
 * Base: /api/assessments/*
 *
 * Flow:
 *  1. Fetch questions  → display Q&A wizard
 *  2. Submit answers   → stored in MySQL (assessments + assessment_answers tables)
 *  3. Get results      → stored in MySQL (assessment_results table)
 *  4. Get recs         → stored in MySQL (career_recommendations table)
 */
import api from './api';

export const assessmentService = {
  // ─── Question Fetching ────────────────────────────────────────────────────

  /**
   * GET /api/assessments/personality/questions
   * Returns 10 active Big-Five personality questions.
   * @returns {QuestionResponse[]}
   */
  getPersonalityQuestions: () => api.get('/assessments/personality/questions'),

  /**
   * GET /api/assessments/skills/questions
   * Returns 8 active skill-rating questions.
   * @returns {QuestionResponse[]}
   */
  getSkillsQuestions: () => api.get('/assessments/skills/questions'),

  /**
   * GET /api/assessments/interest/questions
   * Returns 10 active interest survey questions.
   * @returns {QuestionResponse[]}
   */
  getInterestQuestions: () => api.get('/assessments/interest/questions'),

  // ─── Submit Answers (stores data in MySQL) ────────────────────────────────

  /**
   * POST /api/assessments/personality/submit
   * Saves answers → assessment_answers table.
   * Computes Big-Five scores → assessment_results table.
   * @param {{ [questionId: string]: string }} answers  e.g. { "1": "Agree", "2": "Neutral" }
   * @returns {AssessmentResultResponse}
   */
  submitPersonalityTest: (answers) =>
    api.post('/assessments/personality/submit', { answers }),

  /**
   * POST /api/assessments/skills/submit
   * Saves answers → assessment_answers table.
   * Computes skill scores → assessment_results table.
   * @param {{ [questionId: string]: string }} answers
   * @returns {AssessmentResultResponse}
   */
  submitSkillsTest: (answers) =>
    api.post('/assessments/skills/submit', { answers }),

  /**
   * POST /api/assessments/interest/submit
   * Saves answers → assessment_answers table.
   * Computes interest scores → assessment_results table.
   * After all 3 complete, triggers RecommendationEngine automatically.
   * @param {{ [questionId: string]: string }} answers
   * @returns {AssessmentResultResponse}
   */
  submitInterestSurvey: (answers) =>
    api.post('/assessments/interest/submit', { answers }),

  // ─── Results & Recommendations ────────────────────────────────────────────

  /**
   * GET /api/assessments/results/me
   * Returns the authenticated student's full assessment results.
   * Reads from: assessment_results table.
   * @returns {AssessmentResultResponse}
   */
  getMyResults: () => api.get('/assessments/results/me'),

  /**
   * GET /api/assessments/recommendations
   * Returns top-5 career recommendations ranked by match score.
   * Reads from: career_recommendations table.
   * ⚠️  Requires all 3 assessments to be completed first.
   * @returns {CareerRecommendationResponse[]}
   */
  getRecommendations: () => api.get('/assessments/recommendations'),

  /** @deprecated Use getRecommendations() */
  getCareerRecommendations: () => api.get('/assessments/recommendations'),
};
