# Career Assessment Backend

Spring Boot 3.2.5 · Java 21 · Spring Security + JWT · Spring Data JPA · H2 (dev) / PostgreSQL (prod)

---

## Prerequisites

| Tool | Required Version |
|------|-----------------|
| Java | **21 LTS** (Homebrew: `brew install openjdk@21`) |
| Maven | 3.9+ |
| Node.js (frontend only) | 18+ |

> ⚠️  **Important:** The system Java is version 25. Lombok is NOT yet compatible with Java 25.  
> Always use Java 21 to build/run this project:
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v 21)
> ```

---

## Quick Start (Dev — H2 In-Memory Database)

```bash
# Set Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Run from backend folder
cd career-assessment-backend
mvn spring-boot:run
```

The server starts at **http://localhost:8080**

### Default Admin Credentials (seeded automatically)
| Field | Value |
|-------|-------|
| Email | `admin@careerpath.edu` |
| Password | `Admin@123` |

---

## API Endpoints

### Auth
| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Register student |
| POST | `/api/auth/login` | Student login |
| POST | `/api/auth/admin/login` | Admin login |

### Student (JWT required)
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/students/dashboard` | Dashboard data |
| GET | `/api/students/profile` | Get profile |
| PUT | `/api/students/profile` | Update profile |

### Assessments (JWT required)
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/assessments/personality/questions` | Get personality questions |
| GET | `/api/assessments/skills/questions` | Get skills questions |
| GET | `/api/assessments/interest/questions` | Get interest questions |
| POST | `/api/assessments/personality/submit` | Submit personality answers |
| POST | `/api/assessments/skills/submit` | Submit skills answers |
| POST | `/api/assessments/interest/submit` | Submit interest answers |
| GET | `/api/assessments/results/me` | Get my results |
| GET | `/api/assessments/recommendations` | Get career recommendations |

### Admin (JWT + ADMIN role required)
| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/admin/dashboard` | Dashboard stats |
| GET | `/api/admin/students` | All students |
| GET/PUT/DELETE | `/api/admin/questions/{id}` | Question CRUD |
| GET/POST/PUT/DELETE | `/api/admin/career-paths` | Career paths CRUD |
| GET | `/api/admin/analytics` | Analytics data |

---

## Swagger UI

Open: **http://localhost:8080/swagger-ui.html**

---

## H2 Console (Dev)

Open: **http://localhost:8080/h2-console**  
JDBC URL: `jdbc:h2:file:./data/careerpath`  
Username: `sa` | Password: *(empty)*

---

## Production (PostgreSQL)

1. Create a database: `CREATE DATABASE careerpath_db;`
2. In `application.properties`, comment out H2 block and uncomment PostgreSQL block
3. Set credentials and run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Project Structure

```
src/main/java/com/careerpath/
├── CareerPathApplication.java
├── config/          # SecurityConfig, OpenApiConfig, DataInitializer
├── controller/      # AuthController, StudentController, AssessmentController, AdminController
├── converter/       # MapToJsonConverter
├── dto/
│   ├── request/     # LoginRequest, RegisterRequest, QuestionRequest, ...
│   └── response/    # AuthResponse, ApiResponse, AssessmentResultResponse, ...
├── engine/          # RecommendationEngine
├── exception/       # GlobalExceptionHandler, ResourceNotFoundException, ...
├── model/           # User, Role, Question, Assessment, Career, ...
├── model/enums/     # RoleName, AssessmentType, AssessmentStatus
├── repository/      # All JPA repositories
├── security/        # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
└── service/         # Service interfaces + impl/
```

---

## Running Frontend + Backend Together

```bash
# Terminal 1 — Backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd career-assessment-backend && mvn spring-boot:run

# Terminal 2 — Frontend
cd career-assessment-frontend && npm run dev
```

Frontend runs at http://localhost:3000 (or 3001/3002 if in use)  
Frontend proxies `/api/*` calls → `http://localhost:8080/api/*`
