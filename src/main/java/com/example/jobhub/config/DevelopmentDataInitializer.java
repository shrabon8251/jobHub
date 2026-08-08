package com.example.jobhub.config;

import com.example.jobhub.entity.Application;
import com.example.jobhub.entity.Category;
import com.example.jobhub.entity.Education;
import com.example.jobhub.entity.Experience;
import com.example.jobhub.entity.Interview;
import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.entity.RecruiterProfile;
import com.example.jobhub.entity.SavedJob;
import com.example.jobhub.entity.Skill;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.embeddable.Address;
import com.example.jobhub.entity.enums.ApplicationStatus;
import com.example.jobhub.entity.enums.EmploymentType;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.repository.ApplicationRepository;
import com.example.jobhub.repository.CategoryRepository;
import com.example.jobhub.repository.EducationRepository;
import com.example.jobhub.repository.ExperienceRepository;
import com.example.jobhub.repository.InterviewRepository;
import com.example.jobhub.repository.JobRepository;
import com.example.jobhub.repository.JobSeekerProfileRepository;
import com.example.jobhub.repository.RecruiterProfileRepository;
import com.example.jobhub.repository.SavedJobRepository;
import com.example.jobhub.repository.SkillRepository;
import com.example.jobhub.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("dev")
public class DevelopmentDataInitializer {

    private static final String DEMO_PASSWORD = "ChangeMe123!";

    @Bean
    CommandLineRunner seedDevelopmentData(
            UserRepository users,
            CategoryRepository categories,
            SkillRepository skills,
            RecruiterProfileRepository recruiterProfiles,
            JobSeekerProfileRepository seekerProfiles,
            JobRepository jobs,
            ApplicationRepository applications,
            SavedJobRepository savedJobs,
            InterviewRepository interviews,
            EducationRepository educations,
            ExperienceRepository experiences,
            PasswordEncoder passwordEncoder,
            TransactionTemplate transactionTemplate) {

        return args -> transactionTemplate.executeWithoutResult(status -> {
            Map<String, Category> categoryByName = seedCategories(categories);
            Map<String, Skill> skillByName = seedSkills(skills);
            Map<String, RecruiterProfile> recruiters = seedRecruiters(
                    users,
                    recruiterProfiles,
                    passwordEncoder);
            Map<String, JobSeekerProfile> seekers = seedJobSeekers(
                    users,
                    seekerProfiles,
                    skillByName,
                    passwordEncoder);

            seedAdmin(users, passwordEncoder);
            seedEducation(seekers, educations);
            seedExperience(seekers, experiences);

            Map<String, Job> jobByTitle = seedJobs(jobs, recruiters, categoryByName);
            Map<String, Application> applicationByKey = seedApplications(
                    applications,
                    seekers,
                    jobByTitle);

            seedSavedJobs(savedJobs, seekers, jobByTitle);
            seedInterviews(interviews, applicationByKey);
        });
    }

    private Map<String, Category> seedCategories(CategoryRepository categories) {
        List<String> names = List.of(
                "Software Engineering",
                "Web Development",
                "Data Science",
                "Artificial Intelligence",
                "Cyber Security",
                "UI/UX Design",
                "Mobile Development",
                "DevOps",
                "QA / Testing",
                "Database",
                "Networking");

        Map<String, Category> categoryByName = new LinkedHashMap<>();
        for (String name : names) {
            Category category = categories.findAll().stream()
                    .filter(existing -> existing.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseGet(() -> {
                        Category created = new Category();
                        created.setName(name);
                        return categories.save(created);
                    });

            categoryByName.put(name, category);
        }

        return categoryByName;
    }

    private Map<String, Skill> seedSkills(SkillRepository skills) {
        List<String> names = List.of(
                "java",
                "spring boot",
                "thymeleaf",
                "javascript",
                "react",
                "html",
                "css",
                "sql",
                "mysql",
                "python",
                "data analysis",
                "machine learning",
                "docker",
                "kubernetes",
                "aws",
                "figma",
                "ux research",
                "test automation",
                "cyber security",
                "networking");

        Map<String, Skill> skillByName = new LinkedHashMap<>();
        for (String name : names) {
            String normalizedName = name.toLowerCase(Locale.ROOT);
            Skill skill = skills.findByName(normalizedName)
                    .orElseGet(() -> {
                        Skill created = new Skill();
                        created.setName(normalizedName);
                        return skills.save(created);
                    });

            skillByName.put(normalizedName, skill);
        }

        return skillByName;
    }

    private void seedAdmin(UserRepository users, PasswordEncoder passwordEncoder) {
        getOrCreateUser(
                users,
                "HIRVO Administrator",
                "admin@hirvo.local",
                Role.ADMIN,
                passwordEncoder);
    }

    private Map<String, RecruiterProfile> seedRecruiters(
            UserRepository users,
            RecruiterProfileRepository profiles,
            PasswordEncoder passwordEncoder) {

        List<RecruiterSeed> seeds = List.of(
                new RecruiterSeed(
                        "Northstar Labs",
                        "recruiter@hirvo.local",
                        "Northstar Labs",
                        "Product engineering teams building dependable workflow tools for growing companies.",
                        "https://northstar.example",
                        "+8801700001001",
                        "Banani 11",
                        "Dhaka",
                        "Bangladesh",
                        "1213"),
                new RecruiterSeed(
                        "Brightline Studio",
                        "careers@brightline.local",
                        "Brightline Studio",
                        "A design and web studio helping mission-driven teams launch polished digital products.",
                        "https://brightline.example",
                        "+8801700001002",
                        "Gulshan Avenue",
                        "Dhaka",
                        "Bangladesh",
                        "1212"),
                new RecruiterSeed(
                        "Goodwork Collective",
                        "talent@goodwork.local",
                        "Goodwork Collective",
                        "A distributed technology team focused on data platforms, security, and reliable operations.",
                        "https://goodwork.example",
                        "+8801700001003",
                        "Agrabad Commercial Area",
                        "Chattogram",
                        "Bangladesh",
                        "4100"));

        Map<String, RecruiterProfile> recruiterByEmail = new LinkedHashMap<>();
        for (RecruiterSeed seed : seeds) {
            User user = getOrCreateUser(
                    users,
                    seed.userName(),
                    seed.email(),
                    Role.RECRUITER,
                    passwordEncoder);

            RecruiterProfile profile = profiles.findByUserId(user.getId())
                    .orElseGet(() -> {
                        RecruiterProfile created = new RecruiterProfile();
                        created.setUser(user);
                        created.setCompanyName(seed.companyName());
                        return profiles.save(created);
                    });

            applyRecruiterSeed(profile, seed);
            recruiterByEmail.put(seed.email(), profiles.save(profile));
        }

        return recruiterByEmail;
    }

    private Map<String, JobSeekerProfile> seedJobSeekers(
            UserRepository users,
            JobSeekerProfileRepository profiles,
            Map<String, Skill> skillByName,
            PasswordEncoder passwordEncoder) {

        List<SeekerSeed> seeds = List.of(
                new SeekerSeed(
                        "Maya Rahman",
                        "seeker@hirvo.local",
                        "Frontend engineer focused on accessible, maintainable product interfaces.",
                        "Dhaka",
                        "Bangladesh",
                        "+8801711111001",
                        "https://linkedin.example/maya-rahman",
                        "https://github.example/maya-rahman",
                        List.of("javascript", "react", "html", "css", "ux research")),
                new SeekerSeed(
                        "Arif Hassan",
                        "arif.hassan@hirvo.local",
                        "Backend developer with strong Spring Boot, SQL, and API delivery experience.",
                        "Sylhet",
                        "Bangladesh",
                        "+8801711111002",
                        "https://linkedin.example/arif-hassan",
                        "https://github.example/arif-hassan",
                        List.of("java", "spring boot", "sql", "mysql", "docker")),
                new SeekerSeed(
                        "Samira Khan",
                        "samira.khan@hirvo.local",
                        "Data analyst turning messy business questions into clear dashboards and models.",
                        "Dhaka",
                        "Bangladesh",
                        "+8801711111003",
                        "https://linkedin.example/samira-khan",
                        "https://github.example/samira-khan",
                        List.of("python", "data analysis", "sql", "machine learning")),
                new SeekerSeed(
                        "Nabila Chowdhury",
                        "nabila.chowdhury@hirvo.local",
                        "Product designer blending research, service thinking, and clean interaction design.",
                        "Chattogram",
                        "Bangladesh",
                        "+8801711111004",
                        "https://linkedin.example/nabila-chowdhury",
                        "https://portfolio.example/nabila",
                        List.of("figma", "ux research", "html", "css")),
                new SeekerSeed(
                        "Tanvir Islam",
                        "tanvir.islam@hirvo.local",
                        "DevOps and security-minded engineer improving deployment reliability and observability.",
                        "Rajshahi",
                        "Bangladesh",
                        "+8801711111005",
                        "https://linkedin.example/tanvir-islam",
                        "https://github.example/tanvir-islam",
                        List.of("docker", "kubernetes", "aws", "cyber security", "networking")));

        Map<String, JobSeekerProfile> seekerByEmail = new LinkedHashMap<>();
        for (SeekerSeed seed : seeds) {
            User user = getOrCreateUser(
                    users,
                    seed.fullName(),
                    seed.email(),
                    Role.JOB_SEEKER,
                    passwordEncoder);

            JobSeekerProfile profile = profiles.findByUserId(user.getId())
                    .orElseGet(() -> {
                        JobSeekerProfile created = new JobSeekerProfile();
                        created.setUser(user);
                        created.setFullName(seed.fullName());
                        return profiles.save(created);
                    });

            applySeekerSeed(profile, seed, skillByName);
            seekerByEmail.put(seed.email(), profiles.save(profile));
        }

        return seekerByEmail;
    }

    private Map<String, Job> seedJobs(
            JobRepository jobs,
            Map<String, RecruiterProfile> recruiters,
            Map<String, Category> categories) {

        List<JobSeed> seeds = List.of(
                activeJob("Senior Java Engineer", "recruiter@hirvo.local", "Software Engineering", "Dhaka · Hybrid"),
                activeJob("Spring Boot Developer", "recruiter@hirvo.local", "Software Engineering", "Remote · Bangladesh"),
                activeJob("Frontend Engineer", "careers@brightline.local", "Web Development", "Dhaka · Hybrid"),
                activeJob("Full Stack Web Developer", "careers@brightline.local", "Web Development", "Remote · Asia"),
                activeJob("Data Analyst", "talent@goodwork.local", "Data Science", "Dhaka · On-site"),
                activeJob("Machine Learning Engineer", "talent@goodwork.local", "Artificial Intelligence", "Remote"),
                activeJob("Cyber Security Analyst", "talent@goodwork.local", "Cyber Security", "Chattogram · Hybrid"),
                activeJob("UI/UX Product Designer", "careers@brightline.local", "UI/UX Design", "Dhaka · Hybrid"),
                activeJob("Mobile App Developer", "recruiter@hirvo.local", "Mobile Development", "Remote"),
                activeJob("DevOps Engineer", "talent@goodwork.local", "DevOps", "Remote · Asia"),
                activeJob("QA Automation Engineer", "careers@brightline.local", "QA / Testing", "Dhaka · Hybrid"),
                activeJob("Database Administrator", "talent@goodwork.local", "Database", "Dhaka · On-site"),
                activeJob("Network Support Engineer", "talent@goodwork.local", "Networking", "Chattogram · On-site"),
                activeJob("Product-Minded Backend Engineer", "recruiter@hirvo.local", "Software Engineering", "Dhaka · Hybrid"),
                activeJob("Accessibility-Focused Web Engineer", "careers@brightline.local", "Web Development", "Remote"),
                lifecycleJob(
                        "Paused Platform Engineer",
                        "recruiter@hirvo.local",
                        "DevOps",
                        "Dhaka · Hybrid",
                        EmploymentType.FULL_TIME,
                        JobStatus.PAUSED,
                        35,
                        "Maintain internal platform tooling while the hiring team recalibrates priorities."),
                lifecycleJob(
                        "Closed Product Designer",
                        "careers@brightline.local",
                        "UI/UX Design",
                        "Dhaka · Hybrid",
                        EmploymentType.CONTRACT,
                        JobStatus.CLOSED,
                        18,
                        "A completed search for a designer to support a launch cycle."),
                lifecycleJob(
                        "Expired Data Engineer",
                        "talent@goodwork.local",
                        "Data Science",
                        "Remote",
                        EmploymentType.FULL_TIME,
                        JobStatus.EXPIRED,
                        -7,
                        "A historical listing kept for recruiter and admin lifecycle demos."),
                lifecycleJob(
                        "Part-Time QA Tester",
                        "careers@brightline.local",
                        "QA / Testing",
                        "Remote · Bangladesh",
                        EmploymentType.PART_TIME,
                        JobStatus.ACTIVE,
                        28,
                        "Test responsive workflows and help keep releases calm and predictable."),
                lifecycleJob(
                        "AI Research Intern",
                        "talent@goodwork.local",
                        "Artificial Intelligence",
                        "Dhaka · Hybrid",
                        EmploymentType.INTERNSHIP,
                        JobStatus.ACTIVE,
                        40,
                        "Support applied research experiments, model evaluation, and documentation."));

        Map<String, Job> jobByTitle = new LinkedHashMap<>();
        for (JobSeed seed : seeds) {
            RecruiterProfile recruiter = recruiters.get(seed.recruiterEmail());
            Category category = categories.get(seed.categoryName());

            if (recruiter == null || category == null) {
                continue;
            }

            Job job = jobs.findByTitleAndRecruiterId(seed.title(), recruiter.getId())
                    .orElseGet(() -> {
                        Job created = new Job();
                        created.setRecruiter(recruiter);
                        created.setTitle(seed.title());
                        return created;
                    });

            applyJobSeed(job, seed, category);
            jobByTitle.put(seed.title(), jobs.save(job));
        }

        return jobByTitle;
    }

    private Map<String, Application> seedApplications(
            ApplicationRepository applications,
            Map<String, JobSeekerProfile> seekers,
            Map<String, Job> jobs) {

        List<ApplicationSeed> seeds = List.of(
                application("seeker@hirvo.local", "Frontend Engineer", ApplicationStatus.INTERVIEW),
                application("seeker@hirvo.local", "UI/UX Product Designer", ApplicationStatus.REVIEWING),
                application("arif.hassan@hirvo.local", "Senior Java Engineer", ApplicationStatus.SHORTLISTED),
                application("arif.hassan@hirvo.local", "Spring Boot Developer", ApplicationStatus.APPLIED),
                application("samira.khan@hirvo.local", "Data Analyst", ApplicationStatus.INTERVIEW),
                application("samira.khan@hirvo.local", "Machine Learning Engineer", ApplicationStatus.REVIEWING),
                application("nabila.chowdhury@hirvo.local", "UI/UX Product Designer", ApplicationStatus.SELECTED),
                application("nabila.chowdhury@hirvo.local", "Accessibility-Focused Web Engineer", ApplicationStatus.APPLIED),
                application("tanvir.islam@hirvo.local", "DevOps Engineer", ApplicationStatus.INTERVIEW),
                application("tanvir.islam@hirvo.local", "Cyber Security Analyst", ApplicationStatus.SHORTLISTED),
                application("seeker@hirvo.local", "Mobile App Developer", ApplicationStatus.REJECTED),
                application("arif.hassan@hirvo.local", "Product-Minded Backend Engineer", ApplicationStatus.SELECTED));

        Map<String, Application> applicationByKey = new LinkedHashMap<>();
        for (ApplicationSeed seed : seeds) {
            JobSeekerProfile seeker = seekers.get(seed.seekerEmail());
            Job job = jobs.get(seed.jobTitle());

            if (seeker == null || job == null) {
                continue;
            }

            Application application = applications
                    .findByJobIdAndJobSeekerId(job.getId(), seeker.getId())
                    .orElseGet(() -> {
                        Application created = new Application();
                        created.setJob(job);
                        created.setJobSeeker(seeker);
                        created.setCoverLetter(coverLetterFor(seeker, job));
                        created.setStatus(seed.status());
                        return applications.save(created);
                    });

            applicationByKey.put(applicationKey(seed.seekerEmail(), seed.jobTitle()), application);
        }

        return applicationByKey;
    }

    private void seedSavedJobs(
            SavedJobRepository savedJobs,
            Map<String, JobSeekerProfile> seekers,
            Map<String, Job> jobs) {

        List<SavedJobSeed> seeds = List.of(
                saved("seeker@hirvo.local", "Spring Boot Developer"),
                saved("seeker@hirvo.local", "Accessibility-Focused Web Engineer"),
                saved("arif.hassan@hirvo.local", "DevOps Engineer"),
                saved("arif.hassan@hirvo.local", "Database Administrator"),
                saved("samira.khan@hirvo.local", "AI Research Intern"),
                saved("samira.khan@hirvo.local", "Database Administrator"),
                saved("nabila.chowdhury@hirvo.local", "Frontend Engineer"),
                saved("nabila.chowdhury@hirvo.local", "Part-Time QA Tester"),
                saved("tanvir.islam@hirvo.local", "Cyber Security Analyst"),
                saved("tanvir.islam@hirvo.local", "Network Support Engineer"));

        for (SavedJobSeed seed : seeds) {
            JobSeekerProfile seeker = seekers.get(seed.seekerEmail());
            Job job = jobs.get(seed.jobTitle());

            if (seeker == null || job == null) {
                continue;
            }

            if (!savedJobs.existsByJobIdAndJobSeekerId(job.getId(), seeker.getId())) {
                SavedJob savedJob = new SavedJob();
                savedJob.setJob(job);
                savedJob.setJobSeeker(seeker);
                savedJobs.save(savedJob);
            }
        }
    }

    private void seedInterviews(
            InterviewRepository interviews,
            Map<String, Application> applications) {

        List<InterviewSeed> seeds = List.of(
                interview("seeker@hirvo.local", "Frontend Engineer", 5, "10:30"),
                interview("samira.khan@hirvo.local", "Data Analyst", 7, "14:00"),
                interview("tanvir.islam@hirvo.local", "DevOps Engineer", 10, "16:00"));

        for (InterviewSeed seed : seeds) {
            Application application = applications.get(applicationKey(seed.seekerEmail(), seed.jobTitle()));

            if (application == null || interviews.findByApplicationId(application.getId()).isPresent()) {
                continue;
            }

            Interview interview = new Interview();
            interview.setApplication(application);
            interview.setInterviewDate(LocalDate.now().plusDays(seed.daysFromNow()));
            interview.setInterviewTime(LocalTime.parse(seed.time()));
            interview.setMeetingLink("https://meet.example/hirvo-" + application.getId());
            interview.setNotes("Discuss recent work, team fit, and practical next steps.");
            interviews.save(interview);
        }
    }

    private void seedEducation(
            Map<String, JobSeekerProfile> seekers,
            EducationRepository educations) {

        addEducation(
                educations,
                seekers.get("seeker@hirvo.local"),
                "BSc in Computer Science",
                "BRAC University",
                "Software Engineering",
                2018,
                2022);
        addEducation(
                educations,
                seekers.get("arif.hassan@hirvo.local"),
                "BSc in Software Engineering",
                "Shahjalal University of Science and Technology",
                "Backend Systems",
                2017,
                2021);
        addEducation(
                educations,
                seekers.get("samira.khan@hirvo.local"),
                "MSc in Statistics",
                "University of Dhaka",
                "Data Science",
                2020,
                2022);
        addEducation(
                educations,
                seekers.get("nabila.chowdhury@hirvo.local"),
                "Bachelor of Design",
                "Chittagong University",
                "Interaction Design",
                2016,
                2020);
        addEducation(
                educations,
                seekers.get("tanvir.islam@hirvo.local"),
                "BSc in Computer Engineering",
                "Rajshahi University of Engineering & Technology",
                "Networks and Security",
                2016,
                2020);
    }

    private void seedExperience(
            Map<String, JobSeekerProfile> seekers,
            ExperienceRepository experiences) {

        addExperience(
                experiences,
                seekers.get("seeker@hirvo.local"),
                "CanvasWorks",
                "Frontend Engineer",
                "Built accessible dashboard screens and reusable UI components.",
                LocalDate.now().minusYears(2),
                null);
        addExperience(
                experiences,
                seekers.get("arif.hassan@hirvo.local"),
                "LedgerBee",
                "Backend Developer",
                "Delivered Spring Boot services, SQL optimization, and integration workflows.",
                LocalDate.now().minusYears(3),
                null);
        addExperience(
                experiences,
                seekers.get("samira.khan@hirvo.local"),
                "Insight Harbor",
                "Data Analyst",
                "Created reporting pipelines and product metrics for operations teams.",
                LocalDate.now().minusYears(2),
                null);
        addExperience(
                experiences,
                seekers.get("nabila.chowdhury@hirvo.local"),
                "Bright Desk",
                "Product Designer",
                "Led research synthesis, journey maps, wireframes, and design QA.",
                LocalDate.now().minusYears(4),
                LocalDate.now().minusMonths(4));
        addExperience(
                experiences,
                seekers.get("tanvir.islam@hirvo.local"),
                "OpsWave",
                "DevOps Engineer",
                "Improved CI/CD reliability, containerized services, and incident response practices.",
                LocalDate.now().minusYears(3),
                null);
    }

    private User getOrCreateUser(
            UserRepository users,
            String name,
            String email,
            Role role,
            PasswordEncoder passwordEncoder) {

        return users.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setName(name);
                    user.setEmail(email);
                    user.setRole(role);
                    user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
                    user.setEnabled(true);
                    return users.save(user);
                });
    }

    private void applyRecruiterSeed(RecruiterProfile profile, RecruiterSeed seed) {
        setIfBlankOrPlaceholder(profile::getCompanyName, profile::setCompanyName, seed.companyName());
        setIfBlank(profile::getCompanyDescription, profile::setCompanyDescription, seed.description());
        setIfBlank(profile::getWebsite, profile::setWebsite, seed.website());
        setIfBlank(profile::getPhone, profile::setPhone, seed.phone());
        profile.setAddress(address(seed.street(), seed.city(), seed.country(), seed.postalCode()));
    }

    private void applySeekerSeed(
            JobSeekerProfile profile,
            SeekerSeed seed,
            Map<String, Skill> skillByName) {

        setIfBlankOrPlaceholder(profile::getFullName, profile::setFullName, seed.fullName());
        setIfBlank(profile::getBio, profile::setBio, seed.bio());
        setIfBlank(profile::getPhone, profile::setPhone, seed.phone());
        setIfBlank(profile::getWhatsappNumber, profile::setWhatsappNumber, seed.phone());
        setIfBlank(profile::getLinkedinUrl, profile::setLinkedinUrl, seed.linkedinUrl());

        if (seed.portfolioOrGithubUrl().contains("github")) {
            setIfBlank(profile::getGithubUrl, profile::setGithubUrl, seed.portfolioOrGithubUrl());
        } else {
            setIfBlank(profile::getPortfolioUrl, profile::setPortfolioUrl, seed.portfolioOrGithubUrl());
        }

        profile.setAddress(address(null, seed.city(), seed.country(), null));
        for (String skillName : seed.skills()) {
            Skill skill = skillByName.get(skillName);
            boolean missing = profile.getSkills().stream()
                    .noneMatch(existing -> existing.getName().equals(skillName));

            if (skill != null && missing) {
                profile.getSkills().add(skill);
            }
        }
    }

    private void applyJobSeed(Job job, JobSeed seed, Category category) {
        job.setCategory(category);
        job.setLocation(seed.location());
        job.setEmploymentType(seed.employmentType());
        job.setSalaryMin(seed.salaryMin());
        job.setSalaryMax(seed.salaryMax());
        job.setDeadline(LocalDate.now().plusDays(seed.deadlineOffsetDays()));
        job.setStatus(seed.status());
        job.setDescription(seed.description());
        job.setRequirements("""
                Practical experience with the role domain, clear communication, and care for maintainable work.
                Candidates should be comfortable learning in public and collaborating across disciplines.
                """.trim());
        job.setResponsibilities("""
                Partner with product, design, and operations teammates.
                Ship incremental improvements, document decisions, and keep stakeholders informed.
                """.trim());
    }

    private void addEducation(
            EducationRepository educations,
            JobSeekerProfile seeker,
            String degree,
            String institution,
            String field,
            Integer startYear,
            Integer endYear) {

        if (seeker == null || educations.existsByJobSeekerIdAndDegreeAndInstitution(
                seeker.getId(),
                degree,
                institution)) {
            return;
        }

        Education education = new Education();
        education.setJobSeeker(seeker);
        education.setDegree(degree);
        education.setInstitution(institution);
        education.setFieldOfStudy(field);
        education.setStartYear(startYear);
        education.setEndYear(endYear);
        educations.save(education);
    }

    private void addExperience(
            ExperienceRepository experiences,
            JobSeekerProfile seeker,
            String company,
            String position,
            String description,
            LocalDate startDate,
            LocalDate endDate) {

        if (seeker == null || experiences.existsByJobSeekerIdAndCompanyAndPosition(
                seeker.getId(),
                company,
                position)) {
            return;
        }

        Experience experience = new Experience();
        experience.setJobSeeker(seeker);
        experience.setCompany(company);
        experience.setPosition(position);
        experience.setDescription(description);
        experience.setStartDate(startDate);
        experience.setEndDate(endDate);
        experiences.save(experience);
    }

    private JobSeed activeJob(
            String title,
            String recruiterEmail,
            String categoryName,
            String location) {

        return lifecycleJob(
                title,
                recruiterEmail,
                categoryName,
                location,
                EmploymentType.FULL_TIME,
                JobStatus.ACTIVE,
                45,
                "Join a focused team solving practical customer problems with thoughtful, maintainable work.");
    }

    private JobSeed lifecycleJob(
            String title,
            String recruiterEmail,
            String categoryName,
            String location,
            EmploymentType employmentType,
            JobStatus status,
            int deadlineOffsetDays,
            String description) {

        return new JobSeed(
                title,
                recruiterEmail,
                categoryName,
                location,
                employmentType,
                salaryFor(title, true),
                salaryFor(title, false),
                deadlineOffsetDays,
                status,
                description);
    }

    private BigDecimal salaryFor(String title, boolean minimum) {
        int base = Math.abs(title.hashCode() % 50000) + 70000;
        int value = minimum ? base : base + 45000;
        return BigDecimal.valueOf(value);
    }

    private ApplicationSeed application(
            String seekerEmail,
            String jobTitle,
            ApplicationStatus status) {

        return new ApplicationSeed(seekerEmail, jobTitle, status);
    }

    private SavedJobSeed saved(String seekerEmail, String jobTitle) {
        return new SavedJobSeed(seekerEmail, jobTitle);
    }

    private InterviewSeed interview(
            String seekerEmail,
            String jobTitle,
            int daysFromNow,
            String time) {

        return new InterviewSeed(seekerEmail, jobTitle, daysFromNow, time);
    }

    private String coverLetterFor(JobSeekerProfile seeker, Job job) {
        return "I am interested in the " + job.getTitle()
                + " role at " + job.getRecruiter().getCompanyName()
                + " because it matches my current focus and experience.";
    }

    private String applicationKey(String seekerEmail, String jobTitle) {
        return seekerEmail + "::" + jobTitle;
    }

    private Address address(String street, String city, String country, String postalCode) {
        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        return address;
    }

    private void setIfBlank(
            Supplier<String> getter,
            java.util.function.Consumer<String> setter,
            String value) {

        String current = getter.get();
        if (current == null || current.isBlank()) {
            setter.accept(value);
        }
    }

    private void setIfBlankOrPlaceholder(
            Supplier<String> getter,
            java.util.function.Consumer<String> setter,
            String value) {

        String current = getter.get();
        if (current == null || current.isBlank() || current.startsWith("HIRVO")) {
            setter.accept(value);
        }
    }

    private record RecruiterSeed(
            String userName,
            String email,
            String companyName,
            String description,
            String website,
            String phone,
            String street,
            String city,
            String country,
            String postalCode) {
    }

    private record SeekerSeed(
            String fullName,
            String email,
            String bio,
            String city,
            String country,
            String phone,
            String linkedinUrl,
            String portfolioOrGithubUrl,
            List<String> skills) {
    }

    private record JobSeed(
            String title,
            String recruiterEmail,
            String categoryName,
            String location,
            EmploymentType employmentType,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            int deadlineOffsetDays,
            JobStatus status,
            String description) {
    }

    private record ApplicationSeed(
            String seekerEmail,
            String jobTitle,
            ApplicationStatus status) {
    }

    private record SavedJobSeed(
            String seekerEmail,
            String jobTitle) {
    }

    private record InterviewSeed(
            String seekerEmail,
            String jobTitle,
            int daysFromNow,
            String time) {
    }
}
