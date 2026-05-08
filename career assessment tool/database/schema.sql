-- ===========================================================================
--  Career Assessment Tool  ·  MySQL Workbench Compatible Schema
--  Version  : 4.0  —  JPA-Aligned Rebuild
--  Database : career_assessment_db
--  Engine   : MySQL 8+  |  InnoDB  |  utf8mb4
--  Generated: 2026-02-27
--
--  Tables exactly match Spring Boot JPA entity mappings so that
--  spring.jpa.hibernate.ddl-auto=update finds everything already set up.
--
--  JPA Entity → Table mapping:
--    Role                  → roles
--    User                  → users              (role_id FK, password, enabled)
--    Career                → careers            (salary_range, growth_rate VARCHARs)
--    Career skills         → career_required_skills  (@ElementCollection)
--    Question              → questions          (type, category, order_index, active)
--    Question options      → question_options   (@ElementCollection)
--    Assessment            → assessments        (student_id FK)
--    AssessmentAnswer      → assessment_answers
--    AssessmentResult      → assessment_results (student_id UNIQUE FK)
--    CareerRecommendation  → career_recommendations
--    RecommendationRule    → recommendation_rules
--
--  How to run in MySQL Workbench:
--    1. File → Open SQL Script → select this file
--    2. Click the yellow lightning bolt (Execute All)
--    3. Refresh the Schemas panel — career_assessment_db appears with all tables
-- ===========================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS  = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ===========================================================================
-- DATABASE
-- ===========================================================================
DROP DATABASE IF EXISTS career_assessment_db;

CREATE DATABASE career_assessment_db
    CHARACTER SET utf8mb4
    COLLATE       utf8mb4_unicode_ci;

USE career_assessment_db;

-- ===========================================================================
-- TABLE: roles
--   Maps to JPA entity:  com.careerpath.model.Role
-- ===========================================================================
CREATE TABLE roles (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT pk_roles      PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE      (name)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Application roles: ADMIN and STUDENT';

-- ===========================================================================
-- TABLE: users
--   Maps to JPA entity:  com.careerpath.model.User
--   IMPORTANT: column names must match Hibernate snake_case conversion
--     firstName  → first_name
--     password   → password  (NOT password_hash)
--     enabled    → enabled   (NOT is_enabled)
--     role_id    → direct FK column (NOT a junction table)
-- ===========================================================================
CREATE TABLE users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL  COMMENT 'BCrypt hashed password',
    phone      VARCHAR(20)  NULL,
    grade      VARCHAR(50)  NULL,
    bio        TEXT         NULL,
    city       VARCHAR(100) NULL,
    role_id    BIGINT       NOT NULL,
    enabled    TINYINT(1)   NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_users       PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE      (email),
    CONSTRAINT fk_users_role  FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_users_role (role_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'All platform users — admins and students';

-- ===========================================================================
-- TABLE: careers
--   Maps to JPA entity:  com.careerpath.model.Career
--   IMPORTANT: salary_range and growth_rate are VARCHAR (not DECIMAL)
--              education column is 'education' (not 'education_level')
-- ===========================================================================
CREATE TABLE careers (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(255) NOT NULL,
    industry     VARCHAR(100) NULL,
    description  TEXT         NULL,
    salary_range VARCHAR(100) NULL  COMMENT 'e.g. $60,000 – $150,000',
    growth_rate  VARCHAR(100) NULL  COMMENT 'e.g. 25% (Much Faster than Average)',
    education    VARCHAR(255) NULL  COMMENT 'e.g. Bachelor Degree',
    work_style   VARCHAR(100) NULL  COMMENT 'e.g. Remote, Hybrid, Office',
    status       VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_careers       PRIMARY KEY (id),
    CONSTRAINT uq_careers_title UNIQUE      (title),
    INDEX        idx_careers_industry (industry),
    INDEX        idx_careers_status   (status),
    FULLTEXT KEY ft_careers           (title, description)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Career catalogue used by the recommendation engine';

-- ===========================================================================
-- TABLE: career_required_skills
--   Maps to JPA @ElementCollection on Career.requiredSkills
--   @CollectionTable(name="career_required_skills")
--   @Column(name="skill")
--   @OrderColumn(name="option_order")
-- ===========================================================================
CREATE TABLE career_required_skills (
    career_id    BIGINT       NOT NULL,
    skill        VARCHAR(255) NOT NULL,
    option_order INT          NOT NULL DEFAULT 0,

    CONSTRAINT fk_crs_career FOREIGN KEY (career_id) REFERENCES careers (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_crs_career (career_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Required skills per career (@ElementCollection ordered list)';

-- ===========================================================================
-- TABLE: questions
--   Maps to JPA entity:  com.careerpath.model.Question
--   IMPORTANT:
--     type        → AssessmentType enum stored as VARCHAR (PERSONALITY/SKILLS/INTEREST)
--     category    → category name string (e.g. "Extraversion")
--     order_index → display order
--     active      → TINYINT 1/0
-- ===========================================================================
CREATE TABLE questions (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    text        TEXT         NOT NULL,
    type        VARCHAR(20)  NOT NULL  COMMENT 'PERSONALITY | SKILLS | INTEREST',
    category    VARCHAR(100) NOT NULL  COMMENT 'e.g. Extraversion, Analytical Thinking',
    order_index INT          NOT NULL DEFAULT 0,
    active      TINYINT(1)   NOT NULL DEFAULT 1,

    CONSTRAINT pk_questions PRIMARY KEY (id),
    INDEX idx_q_type        (type),
    INDEX idx_q_category    (category),
    INDEX idx_q_active      (active)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Assessment questions (personality, skills, interest)';

-- ===========================================================================
-- TABLE: question_options
--   Maps to JPA @ElementCollection on Question.options
--   @CollectionTable(name="question_options")
--   @Column(name="option_value")
--   @OrderColumn(name="option_order")
-- ===========================================================================
CREATE TABLE question_options (
    question_id  BIGINT       NOT NULL,
    option_value VARCHAR(255) NOT NULL  COMMENT 'Display text, e.g. Strongly Agree',
    option_order INT          NOT NULL DEFAULT 0,

    CONSTRAINT fk_qo_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_qo_question (question_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Answer options per question (@ElementCollection ordered list)';

-- ===========================================================================
-- TABLE: assessments
--   Maps to JPA entity:  com.careerpath.model.Assessment
--   IMPORTANT: FK column is `student_id` (NOT user_id)
-- ===========================================================================
CREATE TABLE assessments (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    student_id   BIGINT      NOT NULL,
    type         VARCHAR(20) NOT NULL  COMMENT 'PERSONALITY | SKILLS | INTEREST',
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING'  COMMENT 'PENDING | IN_PROGRESS | COMPLETED',
    started_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME    NULL,

    CONSTRAINT pk_assessments   PRIMARY KEY (id),
    CONSTRAINT fk_asmnt_student FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_asmnt_student (student_id),
    INDEX idx_asmnt_type    (type),
    INDEX idx_asmnt_status  (status)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Assessment sessions — one row per attempt per student';

-- ===========================================================================
-- TABLE: assessment_answers
--   Maps to JPA entity:  com.careerpath.model.AssessmentAnswer
--   IMPORTANT: answer_value stores option text; score_value is DOUBLE
-- ===========================================================================
CREATE TABLE assessment_answers (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    assessment_id BIGINT       NOT NULL,
    question_id   BIGINT       NOT NULL,
    answer_value  VARCHAR(255) NOT NULL  COMMENT 'Chosen option text, e.g. Agree',
    score_value   DOUBLE       NOT NULL DEFAULT 0.0  COMMENT 'Computed score 0-100',

    CONSTRAINT pk_assessment_answers PRIMARY KEY (id),
    CONSTRAINT fk_aa_assessment      FOREIGN KEY (assessment_id) REFERENCES assessments (id) ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_aa_question        FOREIGN KEY (question_id)   REFERENCES questions   (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_aa_assessment (assessment_id),
    INDEX idx_aa_question   (question_id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Individual answers submitted by students during an assessment';

-- ===========================================================================
-- TABLE: assessment_results
--   Maps to JPA entity:  com.careerpath.model.AssessmentResult
--   One row per student (UNIQUE on student_id).
--   Score maps stored as JSON TEXT via MapToJsonConverter.
-- ===========================================================================
CREATE TABLE assessment_results (
    id                       BIGINT      NOT NULL AUTO_INCREMENT,
    student_id               BIGINT      NOT NULL,
    -- Personality (Big-Five)
    personality_scores_json  TEXT        NULL  COMMENT 'JSON map: {"Extraversion":75.0,...}',
    personality_type         VARCHAR(10) NULL  COMMENT 'e.g. INTJ, ESFJ',
    personality_completed    TINYINT(1)  NOT NULL DEFAULT 0,
    personality_completed_at DATETIME    NULL,
    -- Skills
    skills_scores_json       TEXT        NULL  COMMENT 'JSON map: {"Analytical Thinking":82.0,...}',
    skills_completed         TINYINT(1)  NOT NULL DEFAULT 0,
    skills_completed_at      DATETIME    NULL,
    -- Interest (RIASEC)
    interest_scores_json     TEXT        NULL  COMMENT 'JSON map: {"Investigative":90.0,...}',
    interest_completed       TINYINT(1)  NOT NULL DEFAULT 0,
    interest_completed_at    DATETIME    NULL,
    -- Audit
    created_at               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_assessment_results PRIMARY KEY (id),
    CONSTRAINT uq_ar_student         UNIQUE      (student_id),
    CONSTRAINT fk_ar_student         FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Aggregated assessment results per student (personality + skills + interest)';

-- ===========================================================================
-- TABLE: career_recommendations
--   Maps to JPA entity:  com.careerpath.model.CareerRecommendation
--   Includes ensemble scoring: rule_score + cosine_score + cluster_score
--   FK column is `student_id` (not user_id)
-- ===========================================================================
CREATE TABLE career_recommendations (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    student_id      BIGINT       NOT NULL,
    career_id       BIGINT       NOT NULL,
    match_score     DOUBLE       NOT NULL DEFAULT 0.0  COMMENT 'Final ensemble score 0-100',
    rule_score      DOUBLE       NULL     DEFAULT 0.0,
    cosine_score    DOUBLE       NULL     DEFAULT 0.0,
    cluster_score   DOUBLE       NULL     DEFAULT 0.0,
    archetype_label VARCHAR(100) NULL     COMMENT 'e.g. Analytical Thinker',
    `rank`          INT          NOT NULL DEFAULT 1    COMMENT '1 = best match',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_career_recommendations PRIMARY KEY (id),
    CONSTRAINT fk_crec_student           FOREIGN KEY (student_id) REFERENCES users   (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_crec_career            FOREIGN KEY (career_id)  REFERENCES careers (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_crec_student      (student_id),
    INDEX idx_crec_career       (career_id),
    INDEX idx_crec_rank         (`rank`),
    INDEX idx_crec_student_rank (student_id, `rank`)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'AI/rule-based career recommendations generated per student';

-- ===========================================================================
-- TABLE: recommendation_rules
--   Maps to JPA entity:  com.careerpath.model.RecommendationRule
--   IMPORTANT:
--     assessment_type  → VARCHAR(20)  (NOT rule_type)
--     active           → TINYINT(1)   (NOT is_active)
--     min_score        → DOUBLE
--     weight           → DOUBLE
-- ===========================================================================
CREATE TABLE recommendation_rules (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    career_id       BIGINT       NOT NULL,
    assessment_type VARCHAR(20)  NOT NULL  COMMENT 'PERSONALITY | SKILLS | INTEREST',
    category        VARCHAR(100) NOT NULL  COMMENT 'Must match Question.category value',
    min_score       DOUBLE       NOT NULL DEFAULT 50.0  COMMENT 'Min score 0-100 to trigger rule',
    weight          DOUBLE       NOT NULL DEFAULT 1.0   COMMENT 'Contribution weight',
    description     VARCHAR(255) NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,

    CONSTRAINT pk_recommendation_rules PRIMARY KEY (id),
    CONSTRAINT fk_rr_career            FOREIGN KEY (career_id) REFERENCES careers (id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_rr_career (career_id),
    INDEX idx_rr_type   (assessment_type),
    INDEX idx_rr_active (active)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Rule-based scoring thresholds linking careers to assessment categories';

-- ===========================================================================
-- EXTRA TABLE: audit_logs  (not a JPA entity — for Workbench analytics)
-- ===========================================================================
CREATE TABLE audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NULL,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NULL,
    entity_id   BIGINT       NULL,
    ip_address  VARCHAR(45)  NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    logged_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),
    CONSTRAINT fk_al_user    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_al_user      (user_id),
    INDEX idx_al_action    (action),
    INDEX idx_al_logged_at (logged_at)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'System-wide audit trail (not a JPA entity)';

-- ===========================================================================
-- SEED DATA
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- roles
-- ---------------------------------------------------------------------------
INSERT INTO roles (name) VALUES ('ADMIN'), ('STUDENT');

-- ---------------------------------------------------------------------------
-- users
--   All users have password: password
--   BCrypt hash ($2a$10$...) of "password" — verified with Spring BCryptPasswordEncoder
-- ---------------------------------------------------------------------------
INSERT INTO users (first_name, last_name, email, password, phone, grade, city, role_id, enabled) VALUES
('System', 'Admin',   'admin@careerpath.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '555-0100', NULL,       'New York', 1, 1),
('Alice',  'Johnson', 'alice@student.edu',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '555-0201', 'Grade 12', 'Chicago',  2, 1),
('Bob',    'Smith',   'bob@student.edu',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '555-0202', 'Grade 11', 'Houston',  2, 1),
('Carol',  'Davis',   'carol@student.edu',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '555-0203', 'Grade 12', 'Phoenix',  2, 1),
('David',  'Wilson',  'david@student.edu',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '555-0204', 'Grade 10', 'Seattle',  2, 1);

-- ---------------------------------------------------------------------------
-- careers
-- ---------------------------------------------------------------------------
INSERT INTO careers (title, industry, description, salary_range, growth_rate, education, work_style, status) VALUES
('Software Engineer',      'Technology',  'Designs, builds, and maintains software applications and systems.',           '$60,000 – $150,000',  '25% (Much Faster than Average)', 'Bachelor Degree', 'Hybrid',  'active'),
('Data Scientist',         'Technology',  'Analyses large datasets to extract actionable business insights using ML.',   '$70,000 – $160,000',  '35% (Much Faster than Average)', 'Master Degree',   'Remote',  'active'),
('UX / UI Designer',       'Design',      'Creates intuitive, visually appealing user interfaces and experiences.',     '$50,000 – $120,000',  '22% (Faster than Average)',      'Bachelor Degree', 'Hybrid',  'active'),
('Clinical Psychologist',  'Healthcare',  'Assesses, diagnoses, and treats mental health conditions in patients.',      '$55,000 – $110,000',  '14% (Faster than Average)',      'Doctoral Degree', 'Office',  'active'),
('Business Analyst',       'Business',    'Bridges business needs with technical solutions using data-driven insights.', '$55,000 – $130,000', '18% (Faster than Average)',      'Bachelor Degree', 'Hybrid',  'active'),
('Mechanical Engineer',    'Engineering', 'Designs, tests, and optimises mechanical systems and physical devices.',     '$55,000 – $120,000',  '7% (Average)',                   'Bachelor Degree', 'Office',  'active'),
('High School Teacher',    'Education',   'Educates, guides, and mentors secondary school students.',                   '$35,000 – $70,000',   '5% (Average)',                   'Bachelor Degree', 'Office',  'active'),
('Entrepreneur / Founder', 'Business',    'Creates and grows new business ventures, often in innovative markets.',      '$20,000 – $500,000+', '15% (Faster than Average)',      'Any',             'Hybrid',  'active');

-- ---------------------------------------------------------------------------
-- career_required_skills
-- ---------------------------------------------------------------------------
INSERT INTO career_required_skills (career_id, skill, option_order) VALUES
(1,'Python Programming',0),(1,'System Design',1),(1,'Problem Solving',2),(1,'Algorithms',3),
(2,'Machine Learning',0),(2,'Python Programming',1),(2,'Statistical Analysis',2),(2,'Data Visualisation',3),
(3,'Figma / Sketch',0),(3,'User Research',1),(3,'Prototyping',2),(3,'Visual Design',3),
(4,'Active Listening',0),(4,'Therapeutic Techniques',1),(4,'Empathy',2),(4,'Report Writing',3),
(5,'Requirements Analysis',0),(5,'SQL & Data Analysis',1),(5,'Stakeholder Communication',2),(5,'Process Mapping',3),
(6,'CAD Software',0),(6,'Thermodynamics',1),(6,'Structural Analysis',2),(6,'Project Management',3),
(7,'Curriculum Design',0),(7,'Classroom Management',1),(7,'Differentiated Instruction',2),(7,'Public Speaking',3),
(8,'Business Strategy',0),(8,'Product Development',1),(8,'Team Leadership',2),(8,'Financial Modelling',3);

-- ---------------------------------------------------------------------------
-- questions  (22 questions: 10 personality, 7 skills, 5 interest)
-- ---------------------------------------------------------------------------
INSERT INTO questions (text, type, category, order_index, active) VALUES
-- PERSONALITY
('I enjoy exploring new and unconventional ideas.',                          'PERSONALITY', 'Openness',            1,  1),
('I am curious about many different topics and fields of knowledge.',        'PERSONALITY', 'Openness',            2,  1),
('I always complete my tasks and assignments on time.',                      'PERSONALITY', 'Conscientiousness',   3,  1),
('I keep my workspace and study materials well organised.',                  'PERSONALITY', 'Conscientiousness',   4,  1),
('I feel energised after spending time with large groups of people.',        'PERSONALITY', 'Extraversion',        5,  1),
('I enjoy meeting new people and starting conversations easily.',            'PERSONALITY', 'Extraversion',        6,  1),
('I genuinely care about the wellbeing and feelings of others.',             'PERSONALITY', 'Agreeableness',       7,  1),
('I am willing to compromise to avoid conflict and help others.',            'PERSONALITY', 'Agreeableness',       8,  1),
('I remain calm and composed when faced with stressful situations.',         'PERSONALITY', 'Emotional Stability', 9,  1),
('I recover quickly from setbacks, criticism, or disappointments.',          'PERSONALITY', 'Emotional Stability', 10, 1),
-- SKILLS
('I enjoy working with numbers, data, and logical puzzles.',                 'SKILLS',      'Analytical Thinking', 11, 1),
('I can break complex problems into smaller, manageable parts.',             'SKILLS',      'Analytical Thinking', 12, 1),
('I express my ideas clearly and confidently when speaking to groups.',      'SKILLS',      'Communication',       13, 1),
('I can write clear, well-structured reports or essays with ease.',          'SKILLS',      'Communication',       14, 1),
('I pick up new software tools or coding languages quickly.',                'SKILLS',      'Technical Aptitude',  15, 1),
('I enjoy troubleshooting and debugging technical problems.',                'SKILLS',      'Technical Aptitude',  16, 1),
('I am comfortable taking charge and directing a group project.',            'SKILLS',      'Leadership',          17, 1),
-- INTEREST
('How much do you enjoy building, repairing, or working with tools?',        'INTEREST',    'Realistic',           18, 1),
('How much do you enjoy scientific experiments or in-depth research?',       'INTEREST',    'Investigative',       19, 1),
('How much time do you spend on creative hobbies like art or writing?',      'INTEREST',    'Artistic',            20, 1),
('How often do you volunteer or help others without expecting anything?',    'INTEREST',    'Social',              21, 1),
('I enjoy persuading others, leading teams, or pitching new ideas.',         'INTEREST',    'Enterprising',        22, 1);

-- ---------------------------------------------------------------------------
-- question_options  (5 options per question, order 0-4)
-- Scoring: option_order / 4 * 100  = score (0, 25, 50, 75, 100)
-- ---------------------------------------------------------------------------
INSERT INTO question_options (question_id, option_value, option_order) VALUES
(1,'Strongly Disagree',0),(1,'Disagree',1),(1,'Neutral',2),(1,'Agree',3),(1,'Strongly Agree',4),
(2,'Strongly Disagree',0),(2,'Disagree',1),(2,'Neutral',2),(2,'Agree',3),(2,'Strongly Agree',4),
(3,'Strongly Disagree',0),(3,'Disagree',1),(3,'Neutral',2),(3,'Agree',3),(3,'Strongly Agree',4),
(4,'Strongly Disagree',0),(4,'Disagree',1),(4,'Neutral',2),(4,'Agree',3),(4,'Strongly Agree',4),
(5,'Strongly Disagree',0),(5,'Disagree',1),(5,'Neutral',2),(5,'Agree',3),(5,'Strongly Agree',4),
(6,'Strongly Disagree',0),(6,'Disagree',1),(6,'Neutral',2),(6,'Agree',3),(6,'Strongly Agree',4),
(7,'Strongly Disagree',0),(7,'Disagree',1),(7,'Neutral',2),(7,'Agree',3),(7,'Strongly Agree',4),
(8,'Strongly Disagree',0),(8,'Disagree',1),(8,'Neutral',2),(8,'Agree',3),(8,'Strongly Agree',4),
(9,'Strongly Disagree',0),(9,'Disagree',1),(9,'Neutral',2),(9,'Agree',3),(9,'Strongly Agree',4),
(10,'Strongly Disagree',0),(10,'Disagree',1),(10,'Neutral',2),(10,'Agree',3),(10,'Strongly Agree',4),
(11,'Strongly Disagree',0),(11,'Disagree',1),(11,'Neutral',2),(11,'Agree',3),(11,'Strongly Agree',4),
(12,'Strongly Disagree',0),(12,'Disagree',1),(12,'Neutral',2),(12,'Agree',3),(12,'Strongly Agree',4),
(13,'Strongly Disagree',0),(13,'Disagree',1),(13,'Neutral',2),(13,'Agree',3),(13,'Strongly Agree',4),
(14,'Strongly Disagree',0),(14,'Disagree',1),(14,'Neutral',2),(14,'Agree',3),(14,'Strongly Agree',4),
(15,'Strongly Disagree',0),(15,'Disagree',1),(15,'Neutral',2),(15,'Agree',3),(15,'Strongly Agree',4),
(16,'Strongly Disagree',0),(16,'Disagree',1),(16,'Neutral',2),(16,'Agree',3),(16,'Strongly Agree',4),
(17,'Strongly Disagree',0),(17,'Disagree',1),(17,'Neutral',2),(17,'Agree',3),(17,'Strongly Agree',4),
(18,'Never',0),(18,'Rarely',1),(18,'Sometimes',2),(18,'Often',3),(18,'Always',4),
(19,'Never',0),(19,'Rarely',1),(19,'Sometimes',2),(19,'Often',3),(19,'Always',4),
(20,'Never',0),(20,'Rarely',1),(20,'Sometimes',2),(20,'Often',3),(20,'Always',4),
(21,'Never',0),(21,'Rarely',1),(21,'Sometimes',2),(21,'Often',3),(21,'Always',4),
(22,'Strongly Disagree',0),(22,'Disagree',1),(22,'Neutral',2),(22,'Agree',3),(22,'Strongly Agree',4);

-- ---------------------------------------------------------------------------
-- recommendation_rules
-- ---------------------------------------------------------------------------
INSERT INTO recommendation_rules (career_id, assessment_type, category, min_score, weight, description) VALUES
-- Software Engineer (id=1)
(1,'PERSONALITY','Openness',            50.0, 1.2, 'Open to learning new technologies'),
(1,'SKILLS',     'Technical Aptitude',  65.0, 2.0, 'Strong technical aptitude is essential'),
(1,'SKILLS',     'Analytical Thinking', 60.0, 1.5, 'Must be able to analyse and debug'),
(1,'INTEREST',   'Investigative',       50.0, 1.0, 'Investigative interest in systems and code'),
-- Data Scientist (id=2)
(2,'SKILLS',     'Analytical Thinking', 75.0, 2.5, 'Top-tier analytical skills are non-negotiable'),
(2,'SKILLS',     'Technical Aptitude',  65.0, 2.0, 'Proficiency in ML tools and programming'),
(2,'PERSONALITY','Openness',            65.0, 1.0, 'Intellectual curiosity drives research quality'),
(2,'INTEREST',   'Investigative',       65.0, 1.5, 'Deep research and data investigation orientation'),
-- UX / UI Designer (id=3)
(3,'SKILLS',     'Communication',       60.0, 1.0, 'Communicating design rationale to stakeholders'),
(3,'INTEREST',   'Artistic',            55.0, 1.5, 'Artistic sensibility drives aesthetic judgement'),
-- Clinical Psychologist (id=4)
(4,'PERSONALITY','Agreeableness',       65.0, 2.0, 'Deep empathy is crucial for patient work'),
(4,'PERSONALITY','Emotional Stability', 65.0, 1.5, 'Must manage emotionally difficult cases calmly'),
(4,'INTEREST',   'Social',              65.0, 1.5, 'Strong people-helping orientation'),
-- Business Analyst (id=5)
(5,'SKILLS',     'Analytical Thinking', 65.0, 2.0, 'Data-driven decision-making mindset'),
(5,'SKILLS',     'Communication',       65.0, 1.5, 'Stakeholder communication skills'),
(5,'INTEREST',   'Enterprising',        50.0, 1.0, 'Business mindset helpful'),
-- Mechanical Engineer (id=6)
(6,'INTEREST',   'Realistic',           65.0, 2.0, 'Hands-on practical and mechanical orientation'),
(6,'SKILLS',     'Analytical Thinking', 55.0, 1.5, 'Engineering calculations and analysis'),
(6,'PERSONALITY','Conscientiousness',   65.0, 1.5, 'Precision and attention to detail'),
-- High School Teacher (id=7)
(7,'INTEREST',   'Social',              65.0, 2.0, 'Passion for helping students learn and grow'),
(7,'SKILLS',     'Communication',       65.0, 2.0, 'Clear and engaging explanation skills'),
(7,'PERSONALITY','Agreeableness',       55.0, 1.0, 'Patience and care for student wellbeing'),
-- Entrepreneur / Founder (id=8)
(8,'INTEREST',   'Enterprising',        75.0, 2.5, 'Strong entrepreneurial drive and vision'),
(8,'PERSONALITY','Openness',            65.0, 1.5, 'Comfort with innovation, ambiguity, and risk'),
(8,'SKILLS',     'Leadership',          65.0, 2.0, 'Ability to lead teams and inspire');

-- ---------------------------------------------------------------------------
-- assessments  (completed for Alice and Carol; in-progress for Bob)
-- ---------------------------------------------------------------------------
INSERT INTO assessments (student_id, type, status, started_at, completed_at) VALUES
(2, 'PERSONALITY', 'COMPLETED',   '2026-02-01 09:00:00', '2026-02-01 09:18:00'),
(2, 'SKILLS',      'COMPLETED',   '2026-02-01 09:20:00', '2026-02-01 09:38:00'),
(2, 'INTEREST',    'COMPLETED',   '2026-02-01 09:40:00', '2026-02-01 09:50:00'),
(4, 'PERSONALITY', 'COMPLETED',   '2026-02-10 14:00:00', '2026-02-10 14:18:00'),
(4, 'SKILLS',      'COMPLETED',   '2026-02-10 14:20:00', '2026-02-10 14:38:00'),
(4, 'INTEREST',    'COMPLETED',   '2026-02-10 14:40:00', '2026-02-10 14:50:00'),
(3, 'PERSONALITY', 'IN_PROGRESS', '2026-02-15 11:00:00', NULL);

-- ---------------------------------------------------------------------------
-- assessment_results  (for Alice user_id=2 and Carol user_id=4)
-- ---------------------------------------------------------------------------
INSERT INTO assessment_results (
    student_id,
    personality_scores_json, personality_type, personality_completed, personality_completed_at,
    skills_scores_json,      skills_completed,  skills_completed_at,
    interest_scores_json,    interest_completed, interest_completed_at
) VALUES
(
  2,
  '{"Openness":88.0,"Conscientiousness":80.0,"Extraversion":60.0,"Agreeableness":70.0,"Emotional Stability":72.0}',
  'INTJ', 1, '2026-02-01 09:18:00',
  '{"Analytical Thinking":93.0,"Communication":76.0,"Technical Aptitude":90.0,"Leadership":65.0}',
  1, '2026-02-01 09:38:00',
  '{"Realistic":45.0,"Investigative":92.0,"Artistic":55.0,"Social":60.0,"Enterprising":58.0}',
  1, '2026-02-01 09:50:00'
),
(
  4,
  '{"Openness":70.0,"Conscientiousness":62.0,"Extraversion":88.0,"Agreeableness":90.0,"Emotional Stability":78.0}',
  'ENFJ', 1, '2026-02-10 14:18:00',
  '{"Analytical Thinking":65.0,"Communication":92.0,"Technical Aptitude":55.0,"Leadership":85.0}',
  1, '2026-02-10 14:38:00',
  '{"Realistic":40.0,"Investigative":55.0,"Artistic":72.0,"Social":94.0,"Enterprising":82.0}',
  1, '2026-02-10 14:50:00'
);

-- ---------------------------------------------------------------------------
-- career_recommendations  (top-3 per completed student)
-- ---------------------------------------------------------------------------
INSERT INTO career_recommendations (student_id, career_id, match_score, rule_score, cosine_score, cluster_score, `rank`) VALUES
(2, 2, 94.5, 96.0, 93.0, 92.0, 1),
(2, 1, 91.2, 92.5, 90.0, 88.0, 2),
(2, 5, 82.0, 80.0, 84.0, 79.0, 3),
(4, 7, 91.0, 93.0, 90.0, 87.0, 1),
(4, 4, 88.5, 87.0, 89.0, 85.0, 2),
(4, 8, 83.0, 81.0, 84.0, 80.0, 3);

-- ---------------------------------------------------------------------------
-- audit_logs
-- ---------------------------------------------------------------------------
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, ip_address, status) VALUES
(1,    'LOGIN',             'user',       1, '192.168.1.1', 'SUCCESS'),
(2,    'LOGIN',             'user',       2, '10.0.0.45',   'SUCCESS'),
(2,    'SUBMIT_ASSESSMENT', 'assessment', 1, '10.0.0.45',   'SUCCESS'),
(2,    'SUBMIT_ASSESSMENT', 'assessment', 2, '10.0.0.45',   'SUCCESS'),
(2,    'SUBMIT_ASSESSMENT', 'assessment', 3, '10.0.0.45',   'SUCCESS'),
(4,    'LOGIN',             'user',       4, '10.0.0.91',   'SUCCESS'),
(4,    'SUBMIT_ASSESSMENT', 'assessment', 4, '10.0.0.91',   'SUCCESS'),
(3,    'LOGIN',             'user',       3, '10.0.0.88',   'SUCCESS'),
(5,    'LOGIN',             'user',       5, '10.0.0.77',   'SUCCESS'),
(NULL, 'SYSTEM_STARTUP',    NULL,      NULL, '127.0.0.1',   'SUCCESS');

-- ===========================================================================
-- VIEWS  (MySQL 8+ compatible — NO FILTER clause)
-- ===========================================================================

CREATE OR REPLACE VIEW vw_top_recommendations AS
SELECT
    u.id                                   AS user_id,
    CONCAT(u.first_name, ' ', u.last_name) AS student_name,
    c.title                                AS career_title,
    c.industry,
    cr.match_score,
    cr.`rank`,
    cr.created_at                          AS recommended_at
FROM career_recommendations cr
    INNER JOIN users   u ON u.id = cr.student_id
    INNER JOIN careers c ON c.id = cr.career_id
WHERE cr.`rank` = 1;

CREATE OR REPLACE VIEW vw_career_demand AS
SELECT
    c.id                                   AS career_id,
    c.title                                AS career_title,
    c.industry,
    COUNT(cr.id)                           AS total_top_picks
FROM careers c
LEFT JOIN career_recommendations cr ON cr.career_id = c.id AND cr.`rank` = 1
GROUP BY c.id, c.title, c.industry
ORDER BY total_top_picks DESC;

CREATE OR REPLACE VIEW vw_assessment_stats AS
SELECT
    type,
    COUNT(*)                                                                  AS total,
    SUM(CASE WHEN status = 'COMPLETED'   THEN 1 ELSE 0 END)                  AS completed,
    SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END)                  AS in_progress,
    ROUND(100.0 * SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) / COUNT(*), 1) AS completion_pct
FROM assessments
GROUP BY type;

CREATE OR REPLACE VIEW vw_student_completion AS
SELECT
    u.id                                   AS user_id,
    CONCAT(u.first_name, ' ', u.last_name) AS full_name,
    u.email,
    u.grade,
    u.city,
    COALESCE(ar.personality_completed, 0)  AS personality_done,
    COALESCE(ar.skills_completed,      0)  AS skills_done,
    COALESCE(ar.interest_completed,    0)  AS interest_done,
    ar.personality_type
FROM users u
LEFT JOIN assessment_results ar ON ar.student_id = u.id
WHERE u.role_id = 2;

-- ===========================================================================
SET FOREIGN_KEY_CHECKS = 1;

-- Verification
SELECT 'Tables'               AS object, COUNT(*) AS count FROM information_schema.TABLES     WHERE TABLE_SCHEMA = 'career_assessment_db' AND TABLE_TYPE = 'BASE TABLE'
UNION ALL
SELECT 'Views',               COUNT(*)                     FROM information_schema.VIEWS      WHERE TABLE_SCHEMA = 'career_assessment_db'
UNION ALL
SELECT 'Users',               COUNT(*)                     FROM users
UNION ALL
SELECT 'Careers',             COUNT(*)                     FROM careers
UNION ALL
SELECT 'Questions',           COUNT(*)                     FROM questions
UNION ALL
SELECT 'Question options',    COUNT(*)                     FROM question_options
UNION ALL
SELECT 'Recommendation rules',COUNT(*)                     FROM recommendation_rules
UNION ALL
SELECT 'Recommendations',     COUNT(*)                     FROM career_recommendations;

-- ===========================================================================
-- END OF SCHEMA  ·  career_assessment_db  ·  v4.0  (JPA-Aligned)
-- ===========================================================================
