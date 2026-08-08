package com.example.jobhub.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.jobhub.entity.Category;
import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.RecruiterProfile;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.enums.EmploymentType;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.entity.enums.Role;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=none")
class JobRepositoryTest {

    private final JobRepository jobs;
    private final EntityManager entityManager;

    @Autowired
    JobRepositoryTest(JobRepository jobs, EntityManager entityManager) {
        this.jobs = jobs;
        this.entityManager = entityManager;
    }

    @Test
    void publicSearchSupportsFiltersAndPagination() {
        RecruiterProfile recruiter = recruiter("repo-search@example.com", "Repository Search Co");
        Category engineering = category("Repository Engineering");
        Category design = category("Repository Design");
        persist(recruiter.getUser(), recruiter, engineering, design);

        persist(job("Java Engineer", "Dhaka", engineering, recruiter, new BigDecimal("100"), new BigDecimal("150")));
        persist(job("Product Designer", "Dhaka", design, recruiter, new BigDecimal("60"), new BigDecimal("90")));
        persist(job("Old Java Engineer", "Dhaka", engineering, recruiter, new BigDecimal("100"), new BigDecimal("150"),
                LocalDate.now().minusDays(1), JobStatus.ACTIVE));
        entityManager.flush();

        Page<Job> firstPage = jobs.searchPublicJobs(
                JobStatus.ACTIVE,
                LocalDate.now(),
                null,
                "dhaka",
                null,
                null,
                new BigDecimal("80"),
                null,
                PageRequest.of(0, 1, Sort.by("createdAt").descending()));

        assertEquals(1, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());

        Page<Job> filtered = jobs.searchPublicJobs(
                JobStatus.ACTIVE,
                LocalDate.now(),
                "java",
                null,
                engineering.getId(),
                EmploymentType.FULL_TIME,
                null,
                null,
                PageRequest.of(0, 10));

        assertEquals(1, filtered.getTotalElements());
        assertEquals("Java Engineer", filtered.getContent().get(0).getTitle());
    }

    private RecruiterProfile recruiter(String email, String companyName) {
        User user = new User();
        user.setName("Repository Recruiter");
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRole(Role.RECRUITER);

        RecruiterProfile recruiter = new RecruiterProfile();
        recruiter.setUser(user);
        recruiter.setCompanyName(companyName);
        return recruiter;
    }

    private Category category(String name) {
        Category category = new Category();
        category.setName(name);
        return category;
    }

    private Job job(String title, String location, Category category, RecruiterProfile recruiter,
                    BigDecimal salaryMin, BigDecimal salaryMax) {
        return job(title, location, category, recruiter, salaryMin, salaryMax,
                LocalDate.now().plusDays(30), JobStatus.ACTIVE);
    }

    private Job job(String title, String location, Category category, RecruiterProfile recruiter,
                    BigDecimal salaryMin, BigDecimal salaryMax, LocalDate deadline, JobStatus status) {
        Job job = new Job();
        job.setTitle(title);
        job.setDescription(title + " description");
        job.setLocation(location);
        job.setEmploymentType(EmploymentType.FULL_TIME);
        job.setDeadline(deadline);
        job.setStatus(status);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setRecruiter(recruiter);
        job.setCategory(category);
        return job;
    }

    private void persist(Object... entities) {
        for (Object entity : entities) {
            entityManager.persist(entity);
        }
    }
}
