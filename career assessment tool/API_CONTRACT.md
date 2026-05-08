# 📋 API Contract — Career Assessment Platform

**Base URL:** `http://localhost:8080/api`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`  
**Content-Type:** `application/json` (all requests)

---

## 🔐 Authentication

All protected endpoints require:
```
Authorization: Bearer <jwt_token>
```

### Standard Response Envelope
Every endpoint returns this wrapper:
```json
{
  "success": true,
  "message": "Human-readable message",
  "data": { }
}
```
On error:
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## 1. 🔑 Authentication APIs

> All routes are **public** — no token required.

---

### `POST /api/auth/register`
Register a new student account.

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com",
  "password": "Password1!",
  "phone": "555-0100",
  "grade": "12th Grade",
  "city": "New York"
}
```
> `firstName`, `lastName`, `email`, `password` — **required**  
> `phone`, `grade`, `city` — optional

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqYW5lLnNtaXRoQGV4YW1wbGUuY29tIiwi...",
    "tokenType": "Bearer",
    "user": {
      "id": 5,
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "phone": "555-0100",
      "grade": "12th Grade",
      "bio": null,
      "city": "New York",
      "role": "STUDENT",
      "createdAt": "2025-01-15T09:30:00"
    }
  }
}
```

**Error `400 Bad Request`:**
```json
{
  "success": false,
  "message": "Email already registered",
  "data": null
}
```

---

### `POST /api/auth/login`
Student login — returns a 24-hour JWT token.

**Request Body:**
```json
{
  "email": "jane.smith@example.com",
  "password": "Password1!"
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 5,
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "phone": "555-0100",
      "grade": "12th Grade",
      "bio": "Aspiring software developer",
      "city": "New York",
      "role": "STUDENT",
      "createdAt": "2025-01-15T09:30:00"
    }
  }
}
```

**Error `401 Unauthorized`:**
```json
{
  "success": false,
  "message": "Invalid email or password",
  "data": null
}
```

---

### `POST /api/auth/admin/login`
Admin login — returns an 8-hour JWT token.

**Request Body:** *(same shape as student login)*
```json
{
  "email": "admin@careerpath.edu",
  "password": "Admin@123"
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Admin login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "firstName": "Admin",
      "lastName": "User",
      "email": "admin@careerpath.edu",
      "phone": null,
      "grade": null,
      "bio": null,
      "city": null,
      "role": "ADMIN",
      "createdAt": "2025-01-01T00:00:00"
    }
  }
}
```

---

## 2. 👤 Student APIs

> All routes require **`Authorization: Bearer <token>`** (STUDENT or ADMIN role).

---

### `GET /api/students/dashboard`
Get the current student's dashboard summary.

**Headers:** `Authorization: Bearer <token>`  
**Request Body:** none

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "studentName": "Jane Smith",
    "personalityCompleted": true,
    "skillsCompleted": false,
    "interestCompleted": false,
    "completedAssessments": 1,
    "totalAssessments": 3,
    "personalityType": "INTJ",
    "topCareerMatch": "Software Engineer",
    "topCareerMatchScore": 87.5,
    "progressPercent": 33
  }
}
```
> `personalityType`, `topCareerMatch`, `topCareerMatchScore` are `null` until the relevant assessments are complete.

---

### `GET /api/students/profile`
Get the authenticated student's full profile.

**Headers:** `Authorization: Bearer <token>`  
**Request Body:** none

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 5,
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phone": "555-0100",
    "grade": "12th Grade",
    "bio": "Aspiring software developer",
    "city": "New York",
    "role": "STUDENT",
    "createdAt": "2025-01-15T09:30:00"
  }
}
```

---

### `PUT /api/students/profile`
Update the authenticated student's profile.

**Headers:** `Authorization: Bearer <token>`

**Request Body:** *(all fields optional — only send what changed)*
```json
{
  "firstName": "Jane",
  "lastName": "Smith-Johnson",
  "phone": "555-0199",
  "grade": "College Freshman",
  "bio": "Passionate about data science and AI.",
  "city": "San Francisco"
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Profile updated",
  "data": {
    "id": 5,
    "firstName": "Jane",
    "lastName": "Smith-Johnson",
    "email": "jane.smith@example.com",
    "phone": "555-0199",
    "grade": "College Freshman",
    "bio": "Passionate about data science and AI.",
    "city": "San Francisco",
    "role": "STUDENT",
    "createdAt": "2025-01-15T09:30:00"
  }
}
```

---

## 3. 📝 Assessment APIs

> All routes require **`Authorization: Bearer <token>`**.

---

### `GET /api/assessments/personality/questions`
Fetch all active Big-Five personality questions.

**Headers:** `Authorization: Bearer <token>`  
**Request Body:** none

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "text": "I enjoy meeting new people and thrive in social environments.",
      "type": "PERSONALITY",
      "category": "Openness",
      "options": [
        "Strongly Agree",
        "Agree",
        "Neutral",
        "Disagree",
        "Strongly Disagree"
      ],
      "orderIndex": 1,
      "active": true
    },
    {
      "id": 2,
      "text": "I prefer routine tasks over new and varied experiences.",
      "type": "PERSONALITY",
      "category": "Conscientiousness",
      "options": [
        "Strongly Agree",
        "Agree",
        "Neutral",
        "Disagree",
        "Strongly Disagree"
      ],
      "orderIndex": 2,
      "active": true
    }
  ]
}
```

---

### `GET /api/assessments/skills/questions`
Fetch all active skills assessment questions.

**Headers:** `Authorization: Bearer <token>`  
**Response shape:** same as personality — `"type": "SKILLS"`, categories include: `"Programming"`, `"Communication"`, `"Analytics"`, `"Creativity"`, `"Leadership"`.

---

### `GET /api/assessments/interest/questions`
Fetch all active interest survey questions.

**Headers:** `Authorization: Bearer <token>`  
**Response shape:** same structure — `"type": "INTEREST"`, categories include: `"Investigative"`, `"Artistic"`, `"Social"`, `"Enterprising"`, `"Conventional"`.

---

### `POST /api/assessments/personality/submit`
Submit answers for the personality assessment.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "answers": {
    "1": "Agree",
    "2": "Neutral",
    "3": "Strongly Agree",
    "4": "Disagree",
    "5": "Agree",
    "6": "Strongly Agree",
    "7": "Neutral",
    "8": "Agree",
    "9": "Strongly Disagree",
    "10": "Agree"
  }
}
```
> **Key** = question `id` (as a string), **Value** = the selected option text from the question's `options` array.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Personality assessment completed",
  "data": {
    "personality": {
      "status": "completed",
      "completedAt": "2025-01-15T10:15:00",
      "type": "INTJ",
      "summary": "You are analytical, strategic, and highly driven.",
      "scores": [
        { "trait": "Openness",          "score": 78.0 },
        { "trait": "Conscientiousness", "score": 85.0 },
        { "trait": "Extraversion",      "score": 42.0 },
        { "trait": "Agreeableness",     "score": 65.0 },
        { "trait": "Neuroticism",       "score": 30.0 }
      ]
    },
    "skills":   { "status": "pending" },
    "interest": { "status": "pending" }
  }
}
```

---

### `POST /api/assessments/skills/submit`
Submit answers for the skills assessment.

**Headers:** `Authorization: Bearer <token>`

**Request Body:** *(same pattern — question IDs 11–18 for default seed data)*
```json
{
  "answers": {
    "11": "Advanced",
    "12": "Intermediate",
    "13": "Beginner",
    "14": "Advanced",
    "15": "Intermediate",
    "16": "Advanced",
    "17": "Intermediate",
    "18": "Beginner"
  }
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Skills assessment completed",
  "data": {
    "personality": {
      "status": "completed",
      "completedAt": "2025-01-15T10:15:00",
      "type": "INTJ",
      "scores": [
        { "trait": "Openness",          "score": 78.0 },
        { "trait": "Conscientiousness", "score": 85.0 },
        { "trait": "Extraversion",      "score": 42.0 },
        { "trait": "Agreeableness",     "score": 65.0 },
        { "trait": "Neuroticism",       "score": 30.0 }
      ]
    },
    "skills": {
      "status": "completed",
      "completedAt": "2025-01-15T10:45:00",
      "scores": [
        { "skill": "Programming",    "score": 88.0 },
        { "skill": "Communication",  "score": 72.0 },
        { "skill": "Analytics",      "score": 91.0 },
        { "skill": "Creativity",     "score": 65.0 },
        { "skill": "Leadership",     "score": 58.0 }
      ]
    },
    "interest": { "status": "pending" }
  }
}
```

---

### `POST /api/assessments/interest/submit`
Submit answers for the interest survey.  
*(Same request shape as skills/personality. After this call, if all 3 are done, the `RecommendationEngine` auto-runs.)*

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Interest survey completed",
  "data": {
    "personality": { "status": "completed", "completedAt": "...", "type": "INTJ", "scores": [ ] },
    "skills":      { "status": "completed", "completedAt": "...", "scores": [ ] },
    "interest": {
      "status": "completed",
      "completedAt": "2025-01-15T11:00:00",
      "scores": [
        { "skill": "Investigative", "score": 82.0 },
        { "skill": "Artistic",      "score": 55.0 },
        { "skill": "Social",        "score": 48.0 },
        { "skill": "Enterprising",  "score": 70.0 },
        { "skill": "Conventional",  "score": 61.0 }
      ]
    }
  }
}
```

---

### `GET /api/assessments/results/me`
Fetch the full assessment results for the authenticated student.

**Headers:** `Authorization: Bearer <token>`  
**Request Body:** none

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "personality": {
      "status": "completed",
      "completedAt": "2025-01-15T10:15:00",
      "type": "INTJ",
      "summary": "You are analytical, strategic, and highly driven.",
      "scores": [
        { "trait": "Openness",          "score": 78.0 },
        { "trait": "Conscientiousness", "score": 85.0 },
        { "trait": "Extraversion",      "score": 42.0 },
        { "trait": "Agreeableness",     "score": 65.0 },
        { "trait": "Neuroticism",       "score": 30.0 }
      ]
    },
    "skills": {
      "status": "completed",
      "completedAt": "2025-01-15T10:45:00",
      "scores": [
        { "skill": "Programming",   "score": 88.0 },
        { "skill": "Communication", "score": 72.0 },
        { "skill": "Analytics",     "score": 91.0 },
        { "skill": "Creativity",    "score": 65.0 },
        { "skill": "Leadership",    "score": 58.0 }
      ]
    },
    "interest": {
      "status": "pending",
      "completedAt": null,
      "scores": []
    }
  }
}
```
> Sections not yet completed return `{ "status": "pending" }` with null/empty fields.

---

### `GET /api/assessments/recommendations`
Get the student's top 5 career recommendations.  
⚠️ **Requires all 3 assessments to be completed first.**

**Headers:** `Authorization: Bearer <token>`  
**Request Body:** none

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 11,
      "rank": 1,
      "matchScore": 92.4,
      "title": "Software Engineer",
      "industry": "Technology",
      "description": "Design, develop, and maintain software systems and applications.",
      "salaryRange": "$85,000 - $150,000",
      "growthRate": "25% (Much faster than average)",
      "education": "Bachelor's in Computer Science or related field",
      "workStyle": "Remote / Hybrid",
      "requiredSkills": ["Programming", "Problem Solving", "Algorithms", "System Design"]
    },
    {
      "id": 12,
      "rank": 2,
      "matchScore": 87.1,
      "title": "Data Scientist",
      "industry": "Technology / Research",
      "description": "Analyze complex data sets to extract insights and support decision-making.",
      "salaryRange": "$90,000 - $160,000",
      "growthRate": "35% (Exceptionally fast)",
      "education": "Master's in Statistics, CS, or Data Science",
      "workStyle": "Remote / Office",
      "requiredSkills": ["Statistics", "Python", "Machine Learning", "Data Visualization"]
    },
    {
      "id": 13,
      "rank": 3,
      "matchScore": 74.8,
      "title": "Research Scientist",
      "industry": "Academia / R&D",
      "description": "Conduct experiments and research to advance scientific knowledge.",
      "salaryRange": "$75,000 - $130,000",
      "growthRate": "8% (Average)",
      "education": "Ph.D. in relevant field",
      "workStyle": "Laboratory / Office",
      "requiredSkills": ["Research Methods", "Critical Thinking", "Technical Writing", "Data Analysis"]
    },
    {
      "id": 14,
      "rank": 4,
      "matchScore": 63.2,
      "title": "Business Analyst",
      "industry": "Business / Consulting",
      "description": "Bridge IT and business by identifying needs and recommending solutions.",
      "salaryRange": "$65,000 - $110,000",
      "growthRate": "14% (Faster than average)",
      "education": "Bachelor's in Business or IT",
      "workStyle": "Hybrid / Office",
      "requiredSkills": ["Analysis", "Communication", "SQL", "Business Strategy"]
    },
    {
      "id": 15,
      "rank": 5,
      "matchScore": 51.0,
      "title": "Product Manager",
      "industry": "Technology",
      "description": "Lead cross-functional teams to define, build, and launch products.",
      "salaryRange": "$100,000 - $180,000",
      "growthRate": "19% (Faster than average)",
      "education": "Bachelor's in CS, Business, or related field",
      "workStyle": "Hybrid / Remote",
      "requiredSkills": ["Roadmapping", "Stakeholder Management", "Agile", "UX Thinking"]
    }
  ]
}
```

**Error `400 Bad Request`** (not all 3 done):
```json
{
  "success": false,
  "message": "Please complete all 3 assessments to view recommendations",
  "data": null
}
```

---

## 4. 🛡️ Admin APIs

> All routes require **`Authorization: Bearer <admin_token>`** (ADMIN role only).  
> Non-admin tokens receive `403 Forbidden`.

---

### `GET /api/admin/dashboard`
Get high-level platform statistics.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "totalStudents": 142,
    "newStudentsThisMonth": 18,
    "completedAssessments": 317,
    "totalCareerPaths": 8,
    "totalQuestions": 28,
    "personalityCompletionRate": 68.3,
    "skillsCompletionRate": 54.9,
    "interestCompletionRate": 47.2,
    "activeCareerPaths": 8
  }
}
```

---

### `GET /api/admin/students`
List all registered student accounts.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 2,
      "firstName": "Alice",
      "lastName": "Johnson",
      "email": "alice@example.com",
      "phone": "555-0101",
      "grade": "11th Grade",
      "bio": null,
      "city": "Chicago",
      "role": "STUDENT",
      "createdAt": "2025-01-10T08:00:00"
    },
    {
      "id": 3,
      "firstName": "Bob",
      "lastName": "Williams",
      "email": "bob@example.com",
      "phone": null,
      "grade": "College Sophomore",
      "bio": "Interested in healthcare careers.",
      "city": "Houston",
      "role": "STUDENT",
      "createdAt": "2025-01-12T14:30:00"
    }
  ]
}
```

---

### `GET /api/admin/students/{id}`
Get a specific student's profile by ID.

**Path Parameter:** `id` — student user ID

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 2,
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice@example.com",
    "phone": "555-0101",
    "grade": "11th Grade",
    "bio": null,
    "city": "Chicago",
    "role": "STUDENT",
    "createdAt": "2025-01-10T08:00:00"
  }
}
```

**Error `404 Not Found`:**
```json
{
  "success": false,
  "message": "Student not found with id: 99",
  "data": null
}
```

---

### `GET /api/admin/students/{id}/results`
Get a specific student's full assessment results.

**Path Parameter:** `id` — student user ID

**Response `200 OK`:** *(same shape as `GET /api/assessments/results/me`)*
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "personality": {
      "status": "completed",
      "completedAt": "2025-01-15T10:15:00",
      "type": "ENFP",
      "scores": [
        { "trait": "Openness",          "score": 90.0 },
        { "trait": "Conscientiousness", "score": 55.0 },
        { "trait": "Extraversion",      "score": 88.0 },
        { "trait": "Agreeableness",     "score": 76.0 },
        { "trait": "Neuroticism",       "score": 45.0 }
      ]
    },
    "skills":   { "status": "pending" },
    "interest": { "status": "pending" }
  }
}
```

---

### `GET /api/admin/questions?type=PERSONALITY`
List all questions, optionally filtered by assessment type.

**Query Parameter:** `type` — `PERSONALITY` | `SKILLS` | `INTEREST` (optional, omit to get all 28)

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "text": "I enjoy meeting new people and thrive in social environments.",
      "type": "PERSONALITY",
      "category": "Openness",
      "options": ["Strongly Agree", "Agree", "Neutral", "Disagree", "Strongly Disagree"],
      "orderIndex": 1,
      "active": true
    },
    {
      "id": 2,
      "text": "I prefer routine tasks over new and varied experiences.",
      "type": "PERSONALITY",
      "category": "Conscientiousness",
      "options": ["Strongly Agree", "Agree", "Neutral", "Disagree", "Strongly Disagree"],
      "orderIndex": 2,
      "active": true
    }
  ]
}
```

---

### `POST /api/admin/questions`
Create a new assessment question.

**Request Body:**
```json
{
  "text": "How comfortable are you presenting in front of large groups?",
  "type": "SKILLS",
  "category": "Communication",
  "options": ["Very Comfortable", "Comfortable", "Neutral", "Uncomfortable", "Very Uncomfortable"],
  "orderIndex": 9,
  "active": true
}
```
> `text`, `type`, `category` — **required**  
> `options`, `orderIndex`, `active` — optional (default: `[]`, `0`, `true`)

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "Question created",
  "data": {
    "id": 29,
    "text": "How comfortable are you presenting in front of large groups?",
    "type": "SKILLS",
    "category": "Communication",
    "options": ["Very Comfortable", "Comfortable", "Neutral", "Uncomfortable", "Very Uncomfortable"],
    "orderIndex": 9,
    "active": true
  }
}
```

---

### `PUT /api/admin/questions/{id}`
Update an existing question by ID.

**Path Parameter:** `id` — question ID  
**Request Body:** *(same shape as POST)*
```json
{
  "text": "How comfortable are you presenting to small teams?",
  "type": "SKILLS",
  "category": "Communication",
  "options": ["Very Comfortable", "Comfortable", "Neutral", "Uncomfortable", "Very Uncomfortable"],
  "orderIndex": 9,
  "active": true
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Question updated",
  "data": {
    "id": 29,
    "text": "How comfortable are you presenting to small teams?",
    "type": "SKILLS",
    "category": "Communication",
    "options": ["Very Comfortable", "Comfortable", "Neutral", "Uncomfortable", "Very Uncomfortable"],
    "orderIndex": 9,
    "active": true
  }
}
```

---

### `DELETE /api/admin/questions/{id}`
Delete a question by ID.

**Path Parameter:** `id` — question ID

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Question deleted",
  "data": null
}
```

---

### `GET /api/admin/career-paths`
List all career paths.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "title": "Software Engineer",
      "industry": "Technology",
      "description": "Design, develop, and maintain software systems and applications.",
      "salaryRange": "$85,000 - $150,000",
      "growthRate": "25% (Much faster than average)",
      "education": "Bachelor's in Computer Science or related field",
      "workStyle": "Remote / Hybrid",
      "requiredSkills": ["Programming", "Problem Solving", "Algorithms", "System Design"],
      "status": "active",
      "createdAt": "2025-01-01T00:00:00"
    },
    {
      "id": 2,
      "title": "Data Scientist",
      "industry": "Technology / Research",
      "description": "Analyze complex data sets to extract insights and support decision-making.",
      "salaryRange": "$90,000 - $160,000",
      "growthRate": "35% (Exceptionally fast)",
      "education": "Master's in Statistics, CS, or Data Science",
      "workStyle": "Remote / Office",
      "requiredSkills": ["Statistics", "Python", "Machine Learning", "Data Visualization"],
      "status": "active",
      "createdAt": "2025-01-01T00:00:00"
    }
  ]
}
```

---

### `POST /api/admin/career-paths`
Create a new career path.

**Request Body:**
```json
{
  "title": "Cybersecurity Analyst",
  "industry": "Technology / Security",
  "description": "Protect computer systems and networks from digital attacks and breaches.",
  "salaryRange": "$80,000 - $140,000",
  "growthRate": "32% (Much faster than average)",
  "education": "Bachelor's in Cybersecurity or Computer Science",
  "workStyle": "Hybrid / Office",
  "requiredSkills": ["Network Security", "Ethical Hacking", "Risk Assessment", "Compliance"],
  "status": "active"
}
```
> Only `title` is **required**. All other fields are optional.

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "Career path created",
  "data": {
    "id": 9,
    "title": "Cybersecurity Analyst",
    "industry": "Technology / Security",
    "description": "Protect computer systems and networks from digital attacks and breaches.",
    "salaryRange": "$80,000 - $140,000",
    "growthRate": "32% (Much faster than average)",
    "education": "Bachelor's in Cybersecurity or Computer Science",
    "workStyle": "Hybrid / Office",
    "requiredSkills": ["Network Security", "Ethical Hacking", "Risk Assessment", "Compliance"],
    "status": "active",
    "createdAt": "2025-01-15T12:00:00"
  }
}
```

---

### `PUT /api/admin/career-paths/{id}`
Update an existing career path by ID.

**Path Parameter:** `id` — career path ID  
**Request Body:** *(same shape as POST)*

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Career path updated",
  "data": {
    "id": 9,
    "title": "Cybersecurity Analyst",
    "status": "active",
    "createdAt": "2025-01-15T12:00:00"
  }
}
```

---

### `DELETE /api/admin/career-paths/{id}`
Delete a career path by ID.

**Path Parameter:** `id` — career path ID

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Career path deleted",
  "data": null
}
```

---

### `GET /api/admin/analytics`
Get full analytics data pre-formatted for Recharts charts.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "monthlyRegistrations": [
      { "month": "Jan", "students": 12 },
      { "month": "Feb", "students": 19 },
      { "month": "Mar", "students": 27 },
      { "month": "Apr", "students": 31 },
      { "month": "May", "students": 24 },
      { "month": "Jun", "students": 18 }
    ],
    "careerDistribution": [
      { "name": "Software Engineer",   "value": 38 },
      { "name": "Data Scientist",      "value": 27 },
      { "name": "UX/UI Designer",      "value": 15 },
      { "name": "Product Manager",     "value": 12 },
      { "name": "Business Analyst",    "value": 10 },
      { "name": "Research Scientist",  "value": 8  },
      { "name": "Teacher",             "value": 6  },
      { "name": "Healthcare Professional", "value": 5 }
    ],
    "personalityTypeDistribution": [
      { "type": "INTJ", "count": 18 },
      { "type": "ENFP", "count": 22 },
      { "type": "INFJ", "count": 14 },
      { "type": "ENTP", "count": 11 },
      { "type": "ISFJ", "count": 9  }
    ],
    "averageSkillScores": [
      { "skill": "Programming",   "avgScore": 65.4 },
      { "skill": "Communication", "avgScore": 71.2 },
      { "skill": "Analytics",     "avgScore": 58.9 },
      { "skill": "Creativity",    "avgScore": 62.7 },
      { "skill": "Leadership",    "avgScore": 54.1 }
    ],
    "averageInterestScores": [
      { "category": "Investigative", "avgScore": 70.3 },
      { "category": "Artistic",      "avgScore": 55.8 },
      { "category": "Social",        "avgScore": 63.1 },
      { "category": "Enterprising",  "avgScore": 58.4 },
      { "category": "Conventional",  "avgScore": 49.7 }
    ],
    "totalAssessmentsTaken": 317,
    "overallCompletionRate": 56.8
  }
}
```

---

## 5. ⚠️ Error Reference

| HTTP Status | Meaning | When it occurs |
|---|---|---|
| `200 OK` | Success | Request processed normally |
| `201 Created` | Resource created | POST to `/questions` or `/career-paths` |
| `400 Bad Request` | Invalid input | Missing required fields, validation failure, duplicate email, assessments not complete |
| `401 Unauthorized` | No/invalid token | Missing `Authorization` header or expired JWT |
| `403 Forbidden` | Wrong role | Student calling admin endpoint |
| `404 Not Found` | Resource missing | Invalid student ID, question ID, career ID |
| `500 Internal Server Error` | Server crash | Unexpected exceptions |

**Validation error shape (`400`):**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "firstName": "First name is required",
    "email": "Please enter a valid email address",
    "password": "Password must be at least 6 characters"
  }
}
```

---

## 6. 🔄 Frontend ↔ Backend Flow

```
Student Registration & Assessment Flow:
────────────────────────────────────────────────────────────────
1.  POST /auth/register          → store token + user in localStorage
2.  GET  /students/dashboard     → render progress cards
3.  GET  /assessments/personality/questions  → render Q&A wizard
4.  POST /assessments/personality/submit     → mark personality done
5.  GET  /assessments/skills/questions       → render Q&A wizard
6.  POST /assessments/skills/submit          → mark skills done
7.  GET  /assessments/interest/questions     → render Q&A wizard
8.  POST /assessments/interest/submit        → triggers RecommendationEngine
9.  GET  /assessments/results/me             → render TestResultPage
10. GET  /assessments/recommendations        → render CareerRecommendationPage

Admin Monitoring Flow:
────────────────────────────────────────────────────────────────
1.  POST /auth/admin/login        → store admin token
2.  GET  /admin/dashboard         → render stats widgets
3.  GET  /admin/students          → render student table
4.  GET  /admin/students/{id}/results → view individual results
5.  GET  /admin/analytics         → render Recharts dashboard
6.  CRUD /admin/questions         → manage question bank
7.  CRUD /admin/career-paths      → manage career catalog
```

---

## 7. 📦 Default Seeded Data (from `DataInitializer`)

| Type | Count | Details |
|---|---|---|
| Roles | 2 | `STUDENT`, `ADMIN` |
| Admin account | 1 | `admin@careerpath.edu` / `Admin@123` |
| Personality questions | 10 | Big Five: Openness, Conscientiousness, Extraversion, Agreeableness, Neuroticism |
| Skills questions | 8 | Programming, Communication, Analytics, Creativity, Leadership |
| Interest questions | 10 | Investigative, Artistic, Social, Enterprising, Conventional |
| Career paths | 8 | Software Engineer, Data Scientist, UX/UI Designer, Product Manager, Research Scientist, Teacher, Business Analyst, Healthcare Professional |
| Recommendation rules | per career | Weighted rules linking career + trait + min-score threshold |
