package com.example.jobhub.service;

import com.example.jobhub.dto.form.ApplicationForm;
import com.example.jobhub.entity.Application;
import com.example.jobhub.entity.Interview;
import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.entity.SavedJob;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.exception.DuplicateApplicationException;
import com.example.jobhub.exception.DuplicateSavedJobException;
import com.example.jobhub.exception.ForbiddenOwnershipException;
import com.example.jobhub.exception.InactiveJobException;
import com.example.jobhub.exception.ResourceNotFoundException;
import com.example.jobhub.repository.ApplicationRepository;
import com.example.jobhub.repository.InterviewRepository;
import com.example.jobhub.repository.JobRepository;
import com.example.jobhub.repository.JobSeekerProfileRepository;
import com.example.jobhub.repository.SavedJobRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final CurrentUserService current;
    private final JobSeekerProfileRepository profiles;
    private final JobRepository jobs;
    private final SavedJobRepository saved;
    private final ApplicationRepository applications;
    private final InterviewRepository interviews;

    private JobSeekerProfile requireSeekerProfile() {
        User user = current.requireCurrentUser();
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new ForbiddenOwnershipException();
        }
        return profiles.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile was not found."));
    }

    @Transactional
    public void save(Long jobId) {
        JobSeekerProfile profile = requireSeekerProfile();
        Job job = activeJob(jobId);
        if (saved.existsByJobIdAndJobSeekerId(jobId, profile.getId())) {
            throw new DuplicateSavedJobException();
        }
        SavedJob savedJob = new SavedJob();
        savedJob.setJob(job);
        savedJob.setJobSeeker(profile);
        saved.save(savedJob);
    }

    @Transactional
    public void removeSaved(Long id) {
        Long userId = current.requireCurrentUser().getId();
        SavedJob savedJob = saved.findByIdAndJobSeekerUserId(id, userId)
                .orElseThrow(ForbiddenOwnershipException::new);
        saved.delete(savedJob);
    }

    public List<SavedJob> saved() {
        return saved.findByJobSeekerUserIdOrderBySavedAtDesc(current.requireCurrentUser().getId());
    }

    @Transactional
    public Application apply(Long jobId, ApplicationForm form) {
        JobSeekerProfile profile = requireSeekerProfile();
        Job job = findJob(jobId);
        ensureJobCanReceiveApplications(job);
        if (applications.existsByJobIdAndJobSeekerId(jobId, profile.getId())) {
            throw new DuplicateApplicationException();
        }
        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(profile);
        application.setCoverLetter(trim(form.getCoverLetter()));
        return applications.save(application);
    }

    public List<Application> applications() {
        return applications.findByJobSeekerUserIdOrderByAppliedAtDesc(current.requireCurrentUser().getId());
    }

    public Application application(Long id) {
        return applications.findByIdAndJobSeekerUserId(id, current.requireCurrentUser().getId())
                .orElseThrow(ForbiddenOwnershipException::new);
    }

    public Interview interview(Long id) {
        return interviews.findByIdAndApplicationJobSeekerUserId(id, current.requireCurrentUser().getId())
                .orElseThrow(ForbiddenOwnershipException::new);
    }

    private Job activeJob(Long jobId) {
        return jobs.findByIdAndStatusAndDeadlineGreaterThanEqual(jobId, JobStatus.ACTIVE, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Job was not found."));
    }

    private Job findJob(Long jobId) {
        return jobs.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job was not found."));
    }

    private void ensureJobCanReceiveApplications(Job job) {
        if (job.getStatus() != JobStatus.ACTIVE || job.getDeadline().isBefore(LocalDate.now())) {
            throw new InactiveJobException();
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
