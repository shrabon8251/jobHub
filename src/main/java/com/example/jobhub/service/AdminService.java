package com.example.jobhub.service;

import com.example.jobhub.dto.form.CategoryForm;
import com.example.jobhub.entity.Category;
import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.exception.ForbiddenOwnershipException;
import com.example.jobhub.exception.ResourceNotFoundException;
import com.example.jobhub.repository.ApplicationRepository;
import com.example.jobhub.repository.CategoryRepository;
import com.example.jobhub.repository.InterviewRepository;
import com.example.jobhub.repository.JobRepository;
import com.example.jobhub.repository.UserRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int PAGE_SIZE = 20;

    private final CurrentUserService current;
    private final UserRepository users;
    private final JobRepository jobs;
    private final CategoryRepository categories;
    private final ApplicationRepository applications;
    private final InterviewRepository interviews;

    private User requireAdmin() {
        User user = current.requireCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenOwnershipException();
        }
        return user;
    }

    public Map<String, Long> statistics() {
        requireAdmin();
        Map<String, Long> statistics = new LinkedHashMap<>();
        statistics.put("Total users", users.count());
        statistics.put("Job seekers", users.countByRole(Role.JOB_SEEKER));
        statistics.put("Recruiters", users.countByRole(Role.RECRUITER));
        statistics.put("Enabled users", users.countByEnabled(true));
        statistics.put("Disabled users", users.countByEnabled(false));
        statistics.put("Total jobs", jobs.count());
        statistics.put("Active jobs", jobs.countByStatus(JobStatus.ACTIVE));
        statistics.put("Draft jobs", jobs.countByStatus(JobStatus.DRAFT));
        statistics.put("Suspended jobs", jobs.countByStatus(JobStatus.SUSPENDED));
        statistics.put("Removed jobs", jobs.countByStatus(JobStatus.REMOVED));
        statistics.put("Applications", applications.count());
        statistics.put("Interviews", interviews.count());
        return statistics;
    }

    public Page<User> users(String query, Role role, Boolean enabled, int page) {
        requireAdmin();
        return users.searchAdminUsers(normalizeQuery(query), role, enabled, adminPage(page));
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        User admin = requireAdmin();
        if (!enabled && admin.getId().equals(id)) {
            throw new IllegalArgumentException("You cannot disable your own admin account.");
        }
        User user = users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found."));
        user.setEnabled(enabled);
    }

    public Page<Job> jobs(String query, JobStatus status, int page) {
        requireAdmin();
        return jobs.searchAdminJobs(normalizeQuery(query), status, adminPage(page));
    }

    public Job job(Long id) {
        requireAdmin();
        return jobs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job was not found."));
    }

    @Transactional
    public void suspend(Long id) {
        Job job = job(id);
        if (job.getStatus() == JobStatus.REMOVED) {
            throw new IllegalArgumentException("A removed job cannot be suspended.");
        }
        job.setStatus(JobStatus.SUSPENDED);
    }

    @Transactional
    public void remove(Long id) {
        job(id).setStatus(JobStatus.REMOVED);
    }

    @Transactional
    public void restore(Long id) {
        Job job = job(id);
        if (job.getStatus() != JobStatus.SUSPENDED && job.getStatus() != JobStatus.REMOVED) {
            throw new IllegalArgumentException("Only suspended or removed jobs can be restored.");
        }
        job.setStatus(job.getDeadline().isAfter(LocalDate.now()) ? JobStatus.ACTIVE : JobStatus.EXPIRED);
    }

    public List<Category> categories() {
        requireAdmin();
        return categories.findAll(Sort.by("name").ascending());
    }

    @Transactional
    public Category createCategory(CategoryForm form) {
        requireAdmin();
        String name = normalizeCategoryName(form.getName());
        if (categories.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category already exists.");
        }
        Category category = new Category();
        category.setName(name);
        return categories.save(category);
    }

    @Transactional
    public void updateCategory(Long id, CategoryForm form) {
        requireAdmin();
        String name = normalizeCategoryName(form.getName());
        if (categories.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Category already exists.");
        }
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category was not found."));
        category.setName(name);
    }

    @Transactional
    public void deleteCategory(Long id) {
        requireAdmin();
        if (!categories.existsById(id)) {
            throw new ResourceNotFoundException("Category was not found.");
        }
        if (jobs.existsByCategoryId(id)) {
            throw new IllegalArgumentException("A category with jobs cannot be deleted.");
        }
        categories.deleteById(id);
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private PageRequest adminPage(int page) {
        return PageRequest.of(Math.max(0, page), PAGE_SIZE, Sort.by("createdAt").descending());
    }

    private String normalizeCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
