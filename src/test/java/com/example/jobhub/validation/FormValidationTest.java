package com.example.jobhub.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.jobhub.dto.form.EducationForm;
import com.example.jobhub.dto.form.ExperienceForm;
import com.example.jobhub.dto.form.InterviewForm;
import com.example.jobhub.dto.form.JobForm;
import com.example.jobhub.dto.form.LoginForm;
import com.example.jobhub.dto.form.ApplicationForm;
import com.example.jobhub.dto.form.CategoryForm;
import com.example.jobhub.dto.form.ProfileForm;
import com.example.jobhub.dto.form.RecruiterProfileForm;
import com.example.jobhub.dto.form.RegistrationForm;
import com.example.jobhub.dto.form.SkillForm;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validatesRegistrationAndLoginFields() {
        RegistrationForm registration = new RegistrationForm();
        registration.setEmail("not-an-email");
        registration.setPassword("short");
        LoginForm login = new LoginForm();
        login.setEmail("not-an-email");

        assertHasViolation(registration, "email");
        assertHasViolation(registration, "password");
        assertHasViolation(login, "email");
        assertHasViolation(login, "password");
    }

    @Test
    void validatesEducationExperienceAndJobRanges() {
        EducationForm education = new EducationForm();
        education.setDegree("Degree");
        education.setInstitution("Institution");
        education.setStartYear(2024);
        education.setEndYear(2023);

        ExperienceForm experience = new ExperienceForm();
        experience.setCompany("Company");
        experience.setPosition("Position");
        experience.setStartDate(LocalDate.now().minusDays(2));
        experience.setEndDate(LocalDate.now().minusDays(3));

        JobForm job = new JobForm();
        job.setTitle("Role");
        job.setDescription("Description");
        job.setLocation("Dhaka");
        job.setSalaryMin(new BigDecimal("10"));
        job.setSalaryMax(new BigDecimal("5"));

        assertHasViolation(education, "startYear");
        assertHasViolation(experience, "startDate");
        assertFalse(validator.validate(job).isEmpty());
    }

    @Test
    void validatesUploadMetadata() {
        MockMultipartFile invalid = new MockMultipartFile(
                "image", "payload.exe", "application/octet-stream", new byte[]{1, 2, 3});
        JobForm job = new JobForm();
        job.setJobImage(invalid);

        Set<ConstraintViolation<JobForm>> violations = validator.validate(job);

        assertTrue(violations.stream().anyMatch(violation -> "jobImage".equals(
                violation.getPropertyPath().toString())));
    }

    @Test
    void validatesInterviewSchedule() {
        InterviewForm interview = new InterviewForm();
        interview.setInterviewDate(LocalDate.now());
        interview.setInterviewTime(java.time.LocalTime.now().minusMinutes(1));

        assertHasViolation(interview, "interviewTime");
    }

    @Test
    void validatesProfileSkillCategoryAndApplicationFields() {
        ProfileForm profile = new ProfileForm();
        profile.setFullName("");
        RecruiterProfileForm recruiter = new RecruiterProfileForm();
        recruiter.setCompanyName("");
        SkillForm skill = new SkillForm();
        skill.setName("");
        CategoryForm category = new CategoryForm();
        category.setName("");
        ApplicationForm application = new ApplicationForm();
        application.setCoverLetter("x".repeat(5001));

        assertFalse(validator.validate(profile).isEmpty());
        assertFalse(validator.validate(recruiter).isEmpty());
        assertFalse(validator.validate(skill).isEmpty());
        assertFalse(validator.validate(category).isEmpty());
        assertFalse(validator.validate(application).isEmpty());
    }

    private void assertHasViolation(Object form, String field) {
        assertTrue(validator.validateProperty(form, field).size() > 0
                        || validator.validate(form).stream().anyMatch(violation -> field.equals(
                        violation.getPropertyPath().toString())),
                "Expected validation error for " + field);
    }
}
