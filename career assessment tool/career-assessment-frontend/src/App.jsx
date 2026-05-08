import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import PublicRoute from './routes/PublicRoute';

// Common Pages
import LandingPage from './pages/common/LandingPage';
import LoginPage from './pages/common/LoginPage';
import RegisterPage from './pages/common/RegisterPage';
import LogoutPage from './pages/common/LogoutPage';
import NotFoundPage from './pages/common/NotFoundPage';
import UnauthorizedPage from './pages/common/UnauthorizedPage';

// Student Pages
import StudentLayout from './components/student/StudentLayout';
import StudentDashboard from './pages/student/StudentDashboard';
import PersonalityTestPage from './pages/student/PersonalityTestPage';
import SkillsAssessmentPage from './pages/student/SkillsAssessmentPage';
import InterestSurveyPage from './pages/student/InterestSurveyPage';
import TestResultPage from './pages/student/TestResultPage';
import CareerRecommendationPage from './pages/student/CareerRecommendationPage';
import ProfilePage from './pages/student/ProfilePage';

// Admin Pages
import AdminLoginPage from './pages/admin/AdminLoginPage';
import AdminLayout from './components/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import QuestionManagement from './pages/admin/QuestionManagement';
import CareerPathManagement from './pages/admin/CareerPathManagement';
import StudentResultsViewer from './pages/admin/StudentResultsViewer';
import AnalyticsDashboard from './pages/admin/AnalyticsDashboard';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3500,
            style: { borderRadius: '12px', fontSize: '14px' },
          }}
        />
        <Routes>
          {/* ── Public / common routes ── */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
          <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
          <Route path="/admin/login" element={<PublicRoute><AdminLoginPage /></PublicRoute>} />
          <Route path="/logout" element={<LogoutPage />} />

          {/* ── Student routes ── */}
          <Route
            path="/student"
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard" element={<StudentDashboard />} />
            <Route path="personality-test" element={<PersonalityTestPage />} />
            <Route path="skills-assessment" element={<SkillsAssessmentPage />} />
            <Route path="interest-survey" element={<InterestSurveyPage />} />
            <Route path="results" element={<TestResultPage />} />
            <Route path="recommendations" element={<CareerRecommendationPage />} />
            <Route path="profile" element={<ProfilePage />} />
          </Route>

          {/* ── Admin routes ── */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard" element={<AdminDashboard />} />
            <Route path="questions" element={<QuestionManagement />} />
            <Route path="career-paths" element={<CareerPathManagement />} />
            <Route path="results" element={<StudentResultsViewer />} />
            <Route path="analytics" element={<AnalyticsDashboard />} />
            <Route path="students" element={<StudentResultsViewer />} />
          </Route>

          {/* ── 404 ── */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
