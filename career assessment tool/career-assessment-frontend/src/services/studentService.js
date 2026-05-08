/**
 * studentService.js
 * Axios calls for the authenticated student's profile and dashboard.
 * Base: /api/students/*
 *
 * All write operations persist data to MySQL via Spring Boot JPA.
 */
import api from './api';

/**
 * Normalizes the backend StudentDashboardResponse into the shape
 * expected by the StudentDashboard component.
 *
 * Backend sends:  completedAssessments, personalityCompleted, skillsCompleted,
 *                 interestCompleted, progressPercent, topCareerMatch, ...
 * Frontend needs: completedTests, profileCompletion, testStatus.personality, ...
 *
 * @param {Object} raw - raw StudentDashboardResponse from backend
 */
const normalizeDashboard = (raw) => ({
  // keep all original fields
  ...raw,
  // map to frontend-expected names
  completedTests:      raw.completedAssessments   ?? 0,
  totalTests:          raw.totalAssessments        ?? 3,
  profileCompletion:   raw.progressPercent         ?? 0,
  recommendationsCount: raw.topCareerMatch ? 5 : 0,
  testStatus: {
    personality: raw.personalityCompleted ? 'completed' : 'pending',
    skills:      raw.skillsCompleted      ? 'completed' : 'pending',
    interest:    raw.interestCompleted    ? 'completed' : 'pending',
  },
});

export const studentService = {
  /**
   * GET /api/students/dashboard
   * Returns student progress stats: completed tests, top career match, etc.
   * Data is computed from the assessments + assessment_results tables.
   * @returns {Object} Normalized dashboard data
   */
  getDashboard: async () => {
    const response = await api.get('/students/dashboard');
    return { ...response, data: normalizeDashboard(response.data) };
  },

  /**
   * GET /api/students/profile
   * Returns the full profile of the authenticated student.
   * Reads from: users table.
   * @returns {UserResponse}
   */
  getProfile: () => api.get('/students/profile'),

  /**
   * PUT /api/students/profile
   * Updates the student's profile fields (firstName, lastName, phone, grade, bio, city).
   * Persists to: users table in MySQL.
   * @param {{ firstName?: string, lastName?: string, phone?: string, grade?: string, bio?: string, city?: string }} data
   * @returns {UserResponse} Updated user object
   */
  updateProfile: (data) => api.put('/students/profile', data),

  /**
   * GET /api/assessments/recommendations
   * Top-5 career recommendations for the student.
   * Reads from: career_recommendations table.
   * ⚠️  All 3 assessments must be complete.
   * @returns {CareerRecommendationResponse[]}
   */
  getCareerRecommendations: () => api.get('/assessments/recommendations'),
};
