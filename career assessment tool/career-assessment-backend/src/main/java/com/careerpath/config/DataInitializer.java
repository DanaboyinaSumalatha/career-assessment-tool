package com.careerpath.config;

import com.careerpath.model.*;
import com.careerpath.model.enums.AssessmentType;
import com.careerpath.model.enums.RoleName;
import com.careerpath.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;
    private final CareerRepository careerRepo;
    private final RecommendationRuleRepository ruleRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepo.count() == 0) {
            log.info("Seeding roles, admin user, questions and career data...");
            initRoles();
            initAdminUser();
            initQuestions();
            initCareers();
            log.info("Seed data loaded successfully.");
        } else {
            log.info("Seed data already exists — skipping.");
        }
    }

    // ── Roles ────────────────────────────────────────────────────────────────

    private void initRoles() {
        roleRepo.save(Role.builder().name(RoleName.ADMIN).build());
        roleRepo.save(Role.builder().name(RoleName.STUDENT).build());
    }

    // ── Admin user ───────────────────────────────────────────────────────────

    private void initAdminUser() {
        Role adminRole = roleRepo.findByName(RoleName.ADMIN).orElseThrow();
        userRepo.save(User.builder()
                .firstName("Mahendra")
                .lastName("Danaboyina")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(adminRole)
                .enabled(true)
                .build());
        log.info("Default admin created → {}", adminEmail);
    }

    // ── Questions ────────────────────────────────────────────────────────────

    private void initQuestions() {
        List<String> agreeOptions = List.of(
                "Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree");
        List<String> abilityOptions = List.of(
                "None", "Beginner", "Basic", "Intermediate", "Advanced");
        List<String> interestOptions = List.of(
                "Not at all", "A little", "Somewhat", "Quite a bit", "Very much");

        // ── Personality (Big Five) ────────────────────────────────────────
        int order = 1;
        questionRepo.save(q("I enjoy being the center of attention at social events.", AssessmentType.PERSONALITY, "Extraversion", agreeOptions, order++));
        questionRepo.save(q("I find it easy to start conversations with strangers.", AssessmentType.PERSONALITY, "Extraversion", agreeOptions, order++));
        questionRepo.save(q("I always follow a plan and stick to deadlines.", AssessmentType.PERSONALITY, "Conscientiousness", agreeOptions, order++));
        questionRepo.save(q("I am well-organized and pay attention to detail.", AssessmentType.PERSONALITY, "Conscientiousness", agreeOptions, order++));
        questionRepo.save(q("I enjoy exploring new ideas and creative approaches.", AssessmentType.PERSONALITY, "Openness", agreeOptions, order++));
        questionRepo.save(q("I am curious about many different things.", AssessmentType.PERSONALITY, "Openness", agreeOptions, order++));
        questionRepo.save(q("I genuinely care about others' feelings and well-being.", AssessmentType.PERSONALITY, "Agreeableness", agreeOptions, order++));
        questionRepo.save(q("I try to avoid conflict and keep the peace.", AssessmentType.PERSONALITY, "Agreeableness", agreeOptions, order++));
        questionRepo.save(q("I remain calm under pressure and handle stress well.", AssessmentType.PERSONALITY, "Emotional Stability", agreeOptions, order++));
        questionRepo.save(q("I rarely feel anxious or worried about things.", AssessmentType.PERSONALITY, "Emotional Stability", agreeOptions, order++));

        // ── Skills ────────────────────────────────────────────────────────
        questionRepo.save(q("Rate your programming / coding ability.", AssessmentType.SKILLS, "Programming", abilityOptions, order++));
        questionRepo.save(q("Rate your data analysis and interpretation ability.", AssessmentType.SKILLS, "Data Analysis", abilityOptions, order++));
        questionRepo.save(q("Rate your written and verbal communication skills.", AssessmentType.SKILLS, "Communication", abilityOptions, order++));
        questionRepo.save(q("Rate your problem-solving and critical thinking ability.", AssessmentType.SKILLS, "Problem Solving", abilityOptions, order++));
        questionRepo.save(q("Rate your leadership and team management skills.", AssessmentType.SKILLS, "Leadership", abilityOptions, order++));
        questionRepo.save(q("Rate your artistic / visual design ability.", AssessmentType.SKILLS, "Design", abilityOptions, order++));
        questionRepo.save(q("Rate your scientific research and analysis ability.", AssessmentType.SKILLS, "Research", abilityOptions, order++));
        questionRepo.save(q("Rate your math and quantitative reasoning ability.", AssessmentType.SKILLS, "Mathematics", abilityOptions, order++));

        // ── Interests (RIASEC) ────────────────────────────────────────────
        questionRepo.save(q("I enjoy working with computers and technology.", AssessmentType.INTEREST, "Investigative", interestOptions, order++));
        questionRepo.save(q("I like conducting experiments or doing scientific research.", AssessmentType.INTEREST, "Investigative", interestOptions, order++));
        questionRepo.save(q("I enjoy creating art, music, or writing.", AssessmentType.INTEREST, "Artistic", interestOptions, order++));
        questionRepo.save(q("I like designing things — web pages, graphics, products.", AssessmentType.INTEREST, "Artistic", interestOptions, order++));
        questionRepo.save(q("I enjoy helping, teaching, or counseling others.", AssessmentType.INTEREST, "Social", interestOptions, order++));
        questionRepo.save(q("I am drawn to careers in education or healthcare.", AssessmentType.INTEREST, "Social", interestOptions, order++));
        questionRepo.save(q("I like leading projects and convincing others.", AssessmentType.INTEREST, "Enterprising", interestOptions, order++));
        questionRepo.save(q("I enjoy business, sales, or entrepreneurial activities.", AssessmentType.INTEREST, "Enterprising", interestOptions, order++));
        questionRepo.save(q("I prefer working with data, records, or structured processes.", AssessmentType.INTEREST, "Conventional", interestOptions, order++));
        questionRepo.save(q("I like building or fixing things with my hands.", AssessmentType.INTEREST, "Realistic", interestOptions, order));
    }

    private Question q(String text, AssessmentType type, String category, List<String> options, int order) {
        return Question.builder()
                .text(text)
                .type(type)
                .category(category)
                .options(options)
                .orderIndex(order)
                .active(true)
                .build();
    }

    // ── Careers + Recommendation Rules ───────────────────────────────────────

    private void initCareers() {
        Career sw = careerRepo.save(Career.builder()
                .title("Software Engineer")
                .industry("Technology")
                .description("Design, develop, and maintain software applications and systems. Work in teams to solve complex technical challenges using programming languages and modern frameworks.")
                .salaryRange("$75,000 – $150,000")
                .growthRate("25% (Much Faster than Average)")
                .education("Bachelor's in Computer Science or related field")
                .workStyle("Hybrid / Remote")
                .requiredSkills(List.of("Programming", "Problem Solving", "Data Structures", "Teamwork", "Version Control"))
                .build());

        Career ds = careerRepo.save(Career.builder()
                .title("Data Scientist")
                .industry("Technology / Analytics")
                .description("Extract insights from large datasets using statistical analysis, machine learning, and data visualization to drive business decisions.")
                .salaryRange("$85,000 – $165,000")
                .growthRate("36% (Much Faster than Average)")
                .education("Bachelor's/Master's in Data Science, Statistics, or CS")
                .workStyle("Hybrid / Remote")
                .requiredSkills(List.of("Data Analysis", "Programming", "Machine Learning", "Statistics", "Communication"))
                .build());

        Career ux = careerRepo.save(Career.builder()
                .title("UX/UI Designer")
                .industry("Technology / Design")
                .description("Create intuitive and visually appealing user experiences for web and mobile applications. Conduct user research and translate insights into wireframes and prototypes.")
                .salaryRange("$65,000 – $120,000")
                .growthRate("13% (Faster than Average)")
                .education("Bachelor's in Design, HCI, or related field")
                .workStyle("Hybrid / In-Office")
                .requiredSkills(List.of("Design", "Communication", "Creativity", "User Research", "Prototyping"))
                .build());

        Career pm = careerRepo.save(Career.builder()
                .title("Product Manager")
                .industry("Technology / Business")
                .description("Define product vision, gather requirements, and coordinate with engineering, design, and marketing teams to deliver successful products to market.")
                .salaryRange("$90,000 – $170,000")
                .growthRate("10% (Faster than Average)")
                .education("Bachelor's in Business, CS, or Engineering")
                .workStyle("Hybrid")
                .requiredSkills(List.of("Leadership", "Communication", "Problem Solving", "Data Analysis", "Project Management"))
                .build());

        Career rs = careerRepo.save(Career.builder()
                .title("Research Scientist")
                .industry("Academia / R&D")
                .description("Conduct original research to advance knowledge in fields such as biology, chemistry, physics, or computer science. Publish findings and collaborate with academic institutions.")
                .salaryRange("$70,000 – $130,000")
                .growthRate("8% (As Fast as Average)")
                .education("PhD or Master's in relevant scientific field")
                .workStyle("In-Office / Lab")
                .requiredSkills(List.of("Research", "Data Analysis", "Mathematics", "Communication", "Critical Thinking"))
                .build());

        Career teacher = careerRepo.save(Career.builder()
                .title("Teacher / Educator")
                .industry("Education")
                .description("Inspire and educate students at primary, secondary, or post-secondary levels. Develop curriculum, assess student progress, and foster a positive learning environment.")
                .salaryRange("$45,000 – $80,000")
                .growthRate("5% (As Fast as Average)")
                .education("Bachelor's in Education + Teaching Certification")
                .workStyle("In-Person")
                .requiredSkills(List.of("Communication", "Leadership", "Patience", "Curriculum Design", "Mentoring"))
                .build());

        Career ba = careerRepo.save(Career.builder()
                .title("Business Analyst")
                .industry("Finance / Consulting")
                .description("Bridge the gap between business needs and technical solutions by analyzing processes, gathering requirements, and recommending data-driven improvements.")
                .salaryRange("$60,000 – $115,000")
                .growthRate("14% (Faster than Average)")
                .education("Bachelor's in Business, Finance, or IT")
                .workStyle("Hybrid")
                .requiredSkills(List.of("Data Analysis", "Communication", "Problem Solving", "Mathematics", "Documentation"))
                .build());

        Career doctor = careerRepo.save(Career.builder()
                .title("Healthcare Professional")
                .industry("Healthcare")
                .description("Provide medical care, diagnose conditions, and develop treatment plans for patients. Roles range from general practitioners to specialists across all medical disciplines.")
                .salaryRange("$80,000 – $250,000")
                .growthRate("13% (Faster than Average)")
                .education("MD / MBBS with residency training")
                .workStyle("In-Person / Clinic / Hospital")
                .requiredSkills(List.of("Communication", "Problem Solving", "Research", "Empathy", "Critical Thinking"))
                .build());

        // ── Recommendation Rules ─────────────────────────────────────────
        saveRules(sw,
                rule(sw, AssessmentType.PERSONALITY, "Openness", 60, 0.25),
                rule(sw, AssessmentType.PERSONALITY, "Conscientiousness", 55, 0.20),
                rule(sw, AssessmentType.SKILLS, "Programming", 50, 0.50),
                rule(sw, AssessmentType.SKILLS, "Problem Solving", 55, 0.40),
                rule(sw, AssessmentType.INTEREST, "Investigative", 55, 0.45));

        saveRules(ds,
                rule(ds, AssessmentType.PERSONALITY, "Openness", 65, 0.25),
                rule(ds, AssessmentType.PERSONALITY, "Conscientiousness", 60, 0.20),
                rule(ds, AssessmentType.SKILLS, "Data Analysis", 55, 0.55),
                rule(ds, AssessmentType.SKILLS, "Mathematics", 55, 0.45),
                rule(ds, AssessmentType.SKILLS, "Programming", 45, 0.30),
                rule(ds, AssessmentType.INTEREST, "Investigative", 60, 0.50));

        saveRules(ux,
                rule(ux, AssessmentType.PERSONALITY, "Openness", 70, 0.35),
                rule(ux, AssessmentType.PERSONALITY, "Agreeableness", 55, 0.20),
                rule(ux, AssessmentType.SKILLS, "Design", 55, 0.55),
                rule(ux, AssessmentType.SKILLS, "Communication", 50, 0.30),
                rule(ux, AssessmentType.INTEREST, "Artistic", 60, 0.50));

        saveRules(pm,
                rule(pm, AssessmentType.PERSONALITY, "Extraversion", 60, 0.30),
                rule(pm, AssessmentType.PERSONALITY, "Conscientiousness", 60, 0.25),
                rule(pm, AssessmentType.SKILLS, "Leadership", 55, 0.50),
                rule(pm, AssessmentType.SKILLS, "Communication", 60, 0.45),
                rule(pm, AssessmentType.INTEREST, "Enterprising", 55, 0.45));

        saveRules(rs,
                rule(rs, AssessmentType.PERSONALITY, "Openness", 70, 0.30),
                rule(rs, AssessmentType.SKILLS, "Research", 60, 0.55),
                rule(rs, AssessmentType.SKILLS, "Data Analysis", 55, 0.40),
                rule(rs, AssessmentType.SKILLS, "Mathematics", 60, 0.40),
                rule(rs, AssessmentType.INTEREST, "Investigative", 65, 0.50));

        saveRules(teacher,
                rule(teacher, AssessmentType.PERSONALITY, "Extraversion", 55, 0.25),
                rule(teacher, AssessmentType.PERSONALITY, "Agreeableness", 65, 0.35),
                rule(teacher, AssessmentType.SKILLS, "Communication", 65, 0.55),
                rule(teacher, AssessmentType.SKILLS, "Leadership", 50, 0.30),
                rule(teacher, AssessmentType.INTEREST, "Social", 65, 0.55));

        saveRules(ba,
                rule(ba, AssessmentType.PERSONALITY, "Conscientiousness", 60, 0.25),
                rule(ba, AssessmentType.SKILLS, "Data Analysis", 55, 0.50),
                rule(ba, AssessmentType.SKILLS, "Communication", 55, 0.40),
                rule(ba, AssessmentType.SKILLS, "Mathematics", 50, 0.35),
                rule(ba, AssessmentType.INTEREST, "Conventional", 55, 0.40),
                rule(ba, AssessmentType.INTEREST, "Enterprising", 50, 0.30));

        saveRules(doctor,
                rule(doctor, AssessmentType.PERSONALITY, "Agreeableness", 65, 0.30),
                rule(doctor, AssessmentType.PERSONALITY, "Conscientiousness", 65, 0.25),
                rule(doctor, AssessmentType.SKILLS, "Communication", 60, 0.40),
                rule(doctor, AssessmentType.SKILLS, "Problem Solving", 60, 0.45),
                rule(doctor, AssessmentType.SKILLS, "Research", 55, 0.30),
                rule(doctor, AssessmentType.INTEREST, "Social", 60, 0.45),
                rule(doctor, AssessmentType.INTEREST, "Investigative", 55, 0.35));
    }

    private RecommendationRule rule(Career career, AssessmentType type,
                                    String category, double minScore, double weight) {
        return RecommendationRule.builder()
                .career(career)
                .assessmentType(type)
                .category(category)
                .minScore(minScore)
                .weight(weight)
                .active(true)
                .build();
    }

    @SafeVarargs
    private void saveRules(Career career, RecommendationRule... rules) {
        for (RecommendationRule rule : rules) {
            ruleRepo.save(rule);
        }
    }
}
