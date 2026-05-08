/**
 * authService.js
 * Axios calls for authentication endpoints.
 * Base: POST /api/auth/*
 *
 * All responses are wrapped by the backend in:
 *   { success: boolean, message: string, data: T }
 * The api.js interceptor unwraps this automatically, so callers
 * receive the inner `data` object directly.
 */
import api from './api';

export const authService = {
  /**
   * Student / user login.
   * POST /api/auth/login
   * @param {{ email: string, password: string }} credentials
   * @returns {{ token: string, tokenType: string, user: UserResponse }}
   */
  login: (credentials) => api.post('/auth/login', credentials),

  /**
   * New student registration.
   * POST /api/auth/register
   * @param {{ firstName: string, lastName: string, email: string, password: string, phone?: string, grade?: string, city?: string }} data
   * @returns {{ token: string, tokenType: string, user: UserResponse }}
   */
  register: (data) => api.post('/auth/register', data),

  /**
   * Admin-only login.
   * POST /api/auth/admin/login
   * @param {{ email: string, password: string }} credentials
   * @returns {{ token: string, tokenType: string, user: UserResponse }}
   */
  adminLogin: (credentials) => api.post('/auth/admin/login', credentials),

  /**
   * Logout — clears server-side session if any.
   * JWT is stateless so this is a client-side no-op;
   * the interceptor in AuthContext handles clearing localStorage.
   * POST /api/auth/logout  (returns 200 even if token is expired)
   */
  logout: () =>
    api.post('/auth/logout').catch(() => Promise.resolve()),
};
