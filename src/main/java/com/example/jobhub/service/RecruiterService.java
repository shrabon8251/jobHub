package com.example.jobhub.service;

import com.example.jobhub.dto.form.JobForm;
import com.example.jobhub.dto.form.RecruiterProfileForm;
import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.RecruiterProfile;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.embeddable.Address;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.exception.ForbiddenOwnershipException;
import com.example.jobhub.exception.ResourceNotFoundException;
import com.example.jobhub.repository.CategoryRepository;
import com.example.jobhub.repository.JobRepository;
import com.example.jobhub.repository.RecruiterProfileRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private static final int JOB_PAGE_SIZE = 10;

    private final CurrentUserService current;
    private final RecruiterProfileRepository recruiters;
    private final JobRepository jobs;
    private final CategoryRepository categories;
    private final MediaService media;

    private User requireRecruiter() {
        User user = current.requireCurrentUser();
        if (user.getRole() != Role.RECRUITER) {
            throw new ForbiddenOwnershipException();
        }
        return user;
    }

    public RecruiterProfile profile() {
        return recruiters.findByUserId(requireRecruiter().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile was not found."));
    }

    @Transactional
    public void updateProfile(RecruiterProfileForm form) {
        RecruiterProfile profile = profile();
        applyProfileFields(profile, form);
        applyAddress(profile, form);
        storeProfileImages(profile, form);
    }

    @Transactional
    public void deleteCompanyLogo() {
        RecruiterProfile profile = profile();
        media.delete(profile.getCompanyPhoto());
        profile.setCompanyPhoto(null);
    }

    @Transactional
    public void deleteCompanyCover() {
        RecruiterProfile profile = profile();
        media.delete(profile.getCompanyCoverPhoto());
        profile.setCompanyCoverPhoto(null);
    }

    public Page<Job> jobs(String keyword, JobStatus status, int page) {
        return jobs.searchByRecruiterUserId(
                requireRecruiter().getId(),
                normalizeKeyword(keyword),
                status,
                PageRequest.of(Math.max(page, 0), JOB_PAGE_SIZE, Sort.by("createdAt").descending()));
    }

    public Job job(Long id) {
        return jobs.findByIdAndRecruiterUserId(id, requireRecruiter().getId())
                .orElseThrow(ForbiddenOwnershipException::new);
    }

    @Transactional
    public Job create(JobForm form) {
        Job job = new Job();
        job.setRecruiter(profile());
        applyJobFields(job, form);
        job.setStatus(JobStatus.ACTIVE);
        return jobs.save(job);
    }

    @Transactional
    public void update(Long id, JobForm form) {
        Job job = job(id);
        ensureEditable(job);
        applyJobFields(job, form);
    }

    @Transactional
    public void delete(Long id) {
        Job job = job(id);
        if (!job.getApplications().isEmpty() || !job.getSavedJobs().isEmpty()) {
            throw new IllegalArgumentException("Jobs with applications or saves cannot be deleted.");
        }
        media.delete(job.getPhoto());
        jobs.delete(job);
    }

    @Transactional
    public void deleteJobImage(Long id) {
        Job job = job(id);
        media.delete(job.getPhoto());
        job.setPhoto(null);
    }

    @Transactional
    public void pause(Long id) {
        transition(job(id), JobStatus.PAUSED, JobStatus.ACTIVE);
    }

    @Transactional
    public void resume(Long id) {
        Job job = job(id);
        if (job.getStatus() != JobStatus.PAUSED) {
            throw new IllegalArgumentException("Only paused jobs can be resumed.");
        }
        if (job.getDeadline().isBefore(LocalDate.now())) {
            job.setStatus(JobStatus.EXPIRED);
            throw new IllegalArgumentException("This job has expired and cannot be resumed.");
        }
        job.setStatus(JobStatus.ACTIVE);
    }

    @Transactional
    public void close(Long id) {
        Job job = job(id);
        if (job.getStatus() == JobStatus.SUSPENDED
                || job.getStatus() == JobStatus.REMOVED
                || job.getStatus() == JobStatus.EXPIRED) {
            throw new IllegalArgumentException("This job cannot be closed in its current status.");
        }
        job.setStatus(JobStatus.CLOSED);
    }

    private void applyProfileFields(RecruiterProfile profile, RecruiterProfileForm form) {
        profile.setCompanyName(form.getCompanyName().trim());
        profile.setCompanyDescription(trim(form.getCompanyDescription()));
        profile.setWebsite(trim(form.getWebsite()));
        profile.setPhone(trim(form.getPhone()));
    }

    private void applyAddress(RecruiterProfile profile, RecruiterProfileForm form) {
        Address address = profile.getAddress() == null ? new Address() : profile.getAddress();
        address.setStreet(trim(form.getStreet()));
        address.setCity(trim(form.getCity()));
        address.setCountry(trim(form.getCountry()));
        address.setPostalCode(trim(form.getPostalCode()));
        profile.setAddress(address);
    }

    private void storeProfileImages(RecruiterProfile profile, RecruiterProfileForm form) {
        if (form.getCompanyLogo() != null && !form.getCompanyLogo().isEmpty()) {
            profile.setCompanyPhoto(media.replace(
                    profile.getCompanyPhoto(), form.getCompanyLogo(), "company-logo"));
        }
        if (form.getCompanyCover() != null && !form.getCompanyCover().isEmpty()) {
            profile.setCompanyCoverPhoto(media.replace(
                    profile.getCompanyCoverPhoto(), form.getCompanyCover(), "company-cover"));
        }
    }

    private void ensureEditable(Job job) {
        if (job.getStatus() == JobStatus.SUSPENDED || job.getStatus() == JobStatus.REMOVED) {
            throw new IllegalArgumentException("This job cannot be edited in its current status.");
        }
    }

    private void transition(Job job, JobStatus target, JobStatus allowedStatus) {
        if (job.getStatus() != allowedStatus) {
            throw new IllegalArgumentException("This lifecycle action is not allowed for the current job status.");
        }
        job.setStatus(target);
    }

    private void applyJobFields(Job job, JobForm form) {
        validateJobValues(form);
        job.setTitle(form.getTitle().trim());
        job.setDescription(form.getDescription().trim());
        job.setRequirements(trim(form.getRequirements()));
        job.setResponsibilities(trim(form.getResponsibilities()));
        job.setSalaryMin(form.getSalaryMin());
        job.setSalaryMax(form.getSalaryMax());
        job.setLocation(form.getLocation().trim());
        job.setEmploymentType(form.getEmploymentType());
        job.setDeadline(form.getDeadline());
        job.setCategory(categories.findById(form.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category was not found.")));
        storeJobImage(job, form);
    }

    private void storeJobImage(Job job, JobForm form) {
        if (form.getJobImage() != null && !form.getJobImage().isEmpty()) {
            job.setPhoto(media.replace(job.getPhoto(), form.getJobImage(), "job"));
        }
    }

    private void validateJobValues(JobForm form) {
        if (form.getSalaryMin() != null && form.getSalaryMax() != null
                && form.getSalaryMin().compareTo(form.getSalaryMax()) > 0) {
            throw new IllegalArgumentException("Minimum salary cannot exceed maximum salary.");
        }
        if (!form.getDeadline().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline must be in the future.");
        }
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
