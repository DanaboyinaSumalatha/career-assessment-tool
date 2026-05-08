# 🎓 CareerPath — Web-Based Career Assessment & Recommendation Platform

A **production-ready React.js frontend** for a web-based career assessment platform designed for students. Features intelligent personality, skills, and interest assessments with AI-powered career path recommendations.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Framework | React 19 + Vite 7 |
| Styling | Tailwind CSS v4 |
| Routing | React Router DOM v6 |
| HTTP Client | Axios |
| Charts | Recharts |
| Icons | Lucide React |
| Notifications | React Hot Toast |
| State | React Context API |

---

## 📁 Project Structure

```
src/
├── components/
│   ├── common/              # Shared UI components
│   │   ├── Navbar.jsx
│   │   ├── Footer.jsx
│   │   ├── Button.jsx
│   │   ├── Card.jsx
│   │   ├── Input.jsx
│   │   ├── Modal.jsx
│   │   ├── Spinner.jsx
│   │   ├── Table.jsx
│   │   ├── Badge.jsx
│   │   └── PageHeader.jsx
│   ├── student/
│   │   ├── StudentLayout.jsx
│   │   └── StudentSidebar.jsx
│   └── admin/
│       ├── AdminLayout.jsx
│       └── AdminSidebar.jsx
├── context/
│   └── AuthContext.jsx      # JWT Auth + Role management
├── pages/
│   ├── common/
│   │   ├── LandingPage.jsx
│   │   ├── LoginPage.jsx
│   │   ├── RegisterPage.jsx
│   │   ├── UnauthorizedPage.jsx
│   │   └── NotFoundPage.jsx
│   ├── student/
│   │   ├── StudentDashboard.jsx
│   │   ├── PersonalityTestPage.jsx
│   │   ├── SkillsAssessmentPage.jsx
│   │   ├── InterestSurveyPage.jsx
│   │   ├── TestResultPage.jsx
│   │   ├── CareerRecommendationPage.jsx
│   │   └── ProfilePage.jsx
│   └── admin/
│       ├── AdminLoginPage.jsx
│       ├── AdminDashboard.jsx
│       ├── QuestionManagement.jsx
│       ├── CareerPathManagement.jsx
│       ├── StudentResultsViewer.jsx
│       └── AnalyticsDashboard.jsx
├── routes/
│   ├── ProtectedRoute.jsx   # Role-based access guard
│   └── PublicRoute.jsx      # Redirect if authenticated
└── services/
    ├── api.js               # Axios instance + interceptors
    ├── authService.js
    ├── assessmentService.js
    ├── studentService.js
    └── adminService.js
```

---

## 🏃 Getting Started

```bash
npm install
npm run dev
```

Open http://localhost:3000 in your browser.

### Build for Production

```bash
npm run build
npm run preview
```

---

## 🔑 Environment Variables

Create a `.env` file:

```env
VITE_API_URL=http://localhost:8080/api
```

---

## 🗺️ Route Map

### Public Routes
| Route | Page |
|---|---|
| `/` | Landing Page |
| `/login` | Student Login |
| `/register` | Student Registration |
| `/admin/login` | Admin Login |

### Student Routes (JWT role: STUDENT)
| Route | Page |
|---|---|
| `/student/dashboard` | Dashboard |
| `/student/personality-test` | Personality Assessment |
| `/student/skills-assessment` | Skills Evaluation |
| `/student/interest-survey` | Interest Survey |
| `/student/results` | Test Results |
| `/student/recommendations` | Career Recommendations |
| `/student/profile` | Profile Management |

### Admin Routes (JWT role: ADMIN)
| Route | Page |
|---|---|
| `/admin/dashboard` | Admin Dashboard |
| `/admin/questions` | Question CRUD |
| `/admin/career-paths` | Career Path CRUD |
| `/admin/students` | Student Management |
| `/admin/results` | Results Viewer |
| `/admin/analytics` | Analytics Dashboard |

---

## 🔐 Authentication Flow

1. POST `/api/auth/login` → receives `{ user, token }`
2. Token stored in `localStorage`
3. Axios interceptor attaches `Authorization: Bearer <token>`
4. 401 response → auto logout + redirect
5. `ProtectedRoute` checks role before rendering

---

## ✨ Key Features

- Dynamic quiz engine with question navigation dots
- Role-based sidebar layouts (Student / Admin)
- Recharts: Radar, Bar, Area, Pie charts
- Full CRUD modals for questions and career paths
- Mock data fallback — works without a backend
- Mobile responsive with drawer sidebars
- Toast notifications on all actions

---

## 📄 License

MIT License
