/**
 * adminService.js
 * Axios calls for all admin-protected endpoints.
 * Base: /api/admin/*
 * Requires: Authorization: Bearer <ADMIN token>
 *
 * All write operations (POST, PUT, DELETE) persist data to MySQL via JPA.
 */
import api from './api';

export const adminService = {
  // ─── Dashboard ─────────────────────────────────────────────────────────────

  /**
   * GET /api/admin/dashboard
   * Platform-wide stats: total students, completion rates, etc.
   * Reads from: users, assessments, careers, questions tables.
   * @returns {AdminDashboardResponse}
   */
  getDashboardStats: () => api.get('/admin/dashboard'),

  // ─── Student Management ────────────────────────────────────────────────────

  /**
   * GET /api/admin/students
   * All registered student accounts.
   * Reads from: users table (role=STUDENT).
   * @returns {UserResponse[]}
   */
  getAllStudents: () => api.get('/admin/students'),

  /**
   * GET /api/admin/students/:id
   * @param {number} id  student user ID
   * @returns {UserResponse}
   */
  getStudentById: (id) => api.get(`/admin/students/${id}`),

  /**
   * GET /api/admin/students/:id/results
   * Full assessment results for one student.
   * Reads from: assessment_results, assessments tables.
   * @param {number} id
   * @returns {AssessmentResultResponse}
   */
  getStudentResults: (id) => api.get(`/admin/students/${id}/results`),

  // ─── Question Management (CRUD → questions table) ─────────────────────────

  /**
   * GET /api/admin/questions[?type=PERSONALITY|SKILLS|INTEREST]
   * Pass type to filter; omit (or pass null/'ALL') to get every question.
   * @param {string|null} type
   * @returns {QuestionResponse[]}
   */
  getAllQuestions: (type) => {
    const valid = ['PERSONALITY', 'SKILLS', 'INTEREST'];
    const qs = valid.includes(type) ? `?type=${type}` : '';
    return api.get(`/admin/questions${qs}`);
  },

  /**
   * POST /api/admin/questions
   * Creates a question. Persists to: questions + question_options tables.
   * @param {{ text: string, type: string, category: string, options: string[], orderIndex?: number, active?: boolean }} data
   * @returns {QuestionResponse}
   */
  createQuestion: (data) => api.post('/admin/questions', data),

  /**
   * PUT /api/admin/questions/:id
   * Updates a question. Persists to: questions + question_options tables.
   * @param {number} id
   * @param {Object} data
   * @returns {QuestionResponse}
   */
  updateQuestion: (id, data) => api.put(`/admin/questions/${id}`, data),

  /**
   * DELETE /api/admin/questions/:id
   * Removes question and its options from MySQL.
   * @param {number} id
   */
  deleteQuestion: (id) => api.delete(`/admin/questions/${id}`),

  // ─── Career Path Management (CRUD → careers table) ────────────────────────

  /**
   * GET /api/admin/career-paths
   * All career paths with required skills.
   * Reads from: careers + career_required_skills tables.
   * @returns {CareerResponse[]}
   */
  getAllCareerPaths: () => api.get('/admin/career-paths'),

  /**
   * POST /api/admin/career-paths
   * Persists to: careers + career_required_skills tables.
   * @param {{ title: string, industry?: string, description?: string, salaryRange?: string, growthRate?: string, education?: string, workStyle?: string, requiredSkills?: string[], status?: string }} data
   * @returns {CareerResponse}
   */
  createCareerPath: (data) => api.post('/admin/career-paths', data),

  /**
   * PUT /api/admin/career-paths/:id
   * @param {number} id
   * @param {Object} data
   * @returns {CareerResponse}
   */
  updateCareerPath: (id, data) => api.put(`/admin/career-paths/${id}`, data),

  /**
   * DELETE /api/admin/career-paths/:id
   * @param {number} id
   */
  deleteCareerPath: (id) => api.delete(`/admin/career-paths/${id}`),

  // ─── Analytics ─────────────────────────────────────────────────────────────

  /**
   * GET /api/admin/analytics
   * Recharts-ready data: monthly registrations, career distribution,
   * personality types, skill scores, interest scores.
   * @returns {AnalyticsResponse}
   */
  getAnalytics: () => api.get('/admin/analytics'),

  // ─── ML Model ──────────────────────────────────────────────────────────────

  /**
   * POST /api/admin/ml/retrain
   * Triggers K-Means retraining on completed assessment data.
   * Source data: assessment_results table.
   * @returns {MLTrainingResponse}
   */
  retrainModel: () => api.post('/admin/ml/retrain'),

  /**
   * GET /api/admin/ml/status
   * Current ML model state: trained/untrained, cluster count, last run time.
   * @returns {MLTrainingResponse}
   */
  getModelStatus: () => api.get('/admin/ml/status'),
};
