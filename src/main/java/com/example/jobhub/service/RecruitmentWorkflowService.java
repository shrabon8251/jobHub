package com.example.jobhub.service;

import com.example.jobhub.dto.form.InterviewForm;
import com.example.jobhub.entity.Application;
import com.example.jobhub.entity.Interview;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.enums.ApplicationStatus;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.exception.ForbiddenOwnershipException;
import com.example.jobhub.repository.ApplicationRepository;
import com.example.jobhub.repository.InterviewRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecruitmentWorkflowService {

    private final CurrentUserService current;
    private final ApplicationRepository applications;
    private final InterviewRepository interviews;

    private User requireRecruiter() {
        User user = current.requireCurrentUser();
        if (user.getRole() != Role.RECRUITER) {
            throw new ForbiddenOwnershipException();
        }
        return user;
    }

    public List<Application> applications() {
        return applications.findByJobRecruiterUserIdOrderByAppliedAtDesc(requireRecruiter().getId());
    }

    public Application application(Long id) {
        return applications.findByIdAndJobRecruiterUserId(id, requireRecruiter().getId())
                .orElseThrow(ForbiddenOwnershipException::new);
    }

    @Transactional
    public void review(Long id) {
        move(application(id), ApplicationStatus.REVIEWING);
    }

    @Transactional
    public void shortlist(Long id) {
        move(application(id), ApplicationStatus.SHORTLISTED);
    }

    @Transactional
    public void select(Long id) {
        move(application(id), ApplicationStatus.SELECTED);
    }

    @Transactional
    public void reject(Long id) {
        Application application = application(id);
        if (isTerminal(application.getStatus())) {
            throw new IllegalArgumentException("This application is already in a terminal state.");
        }
        application.setStatus(ApplicationStatus.REJECTED);
    }

    @Transactional
    public Interview schedule(Long applicationId, InterviewForm form) {
        Application application = application(applicationId);
        ensureShortlisted(application);
        ensureNoExistingInterview(applicationId);

        Interview interview = new Interview();
        interview.setApplication(application);
        applyInterviewFields(interview, form);
        application.setStatus(ApplicationStatus.INTERVIEW);
        return interviews.save(interview);
    }

    public Interview interview(Long id) {
        return interviews.findByIdAndApplicationJobRecruiterUserId(id, requireRecruiter().getId())
                .orElseThrow(ForbiddenOwnershipException::new);
    }

    @Transactional
    public void reschedule(Long id, InterviewForm form) {
        applyInterviewFields(interview(id), form);
    }

    @Transactional
    public void cancel(Long id) {
        Interview interview = interview(id);
        Application application = interview.getApplication();
        if (application.getStatus() != ApplicationStatus.INTERVIEW) {
            throw new IllegalArgumentException("Only an active interview can be cancelled.");
        }
        application.setStatus(ApplicationStatus.SHORTLISTED);
        interviews.delete(interview);
    }

    private void move(Application application, ApplicationStatus target) {
        if (!isAllowedTransition(application.getStatus(), target)) {
            throw new IllegalArgumentException("Invalid application status transition.");
        }
        application.setStatus(target);
    }

    private boolean isAllowedTransition(ApplicationStatus from, ApplicationStatus to) {
        return (from == ApplicationStatus.APPLIED && to == ApplicationStatus.REVIEWING)
                || (from == ApplicationStatus.REVIEWING && to == ApplicationStatus.SHORTLISTED)
                || (from == ApplicationStatus.INTERVIEW && to == ApplicationStatus.SELECTED);
    }

    private boolean isTerminal(ApplicationStatus status) {
        return status == ApplicationStatus.SELECTED || status == ApplicationStatus.REJECTED;
    }

    private void ensureShortlisted(Application application) {
        if (application.getStatus() != ApplicationStatus.SHORTLISTED) {
            throw new IllegalArgumentException("Only shortlisted applications can be scheduled for an interview.");
        }
    }

    private void ensureNoExistingInterview(Long applicationId) {
        if (interviews.findByApplicationId(applicationId).isPresent()) {
            throw new IllegalArgumentException("This application already has an interview.");
        }
    }

    private void applyInterviewFields(Interview interview, InterviewForm form) {
        validateInterviewTime(form);
        interview.setInterviewDate(form.getInterviewDate());
        interview.setInterviewTime(form.getInterviewTime());
        interview.setMeetingLink(trim(form.getMeetingLink()));
        interview.setNotes(trim(form.getNotes()));
    }

    private void validateInterviewTime(InterviewForm form) {
        if (form.getInterviewDate().isEqual(LocalDate.now())
                && form.getInterviewTime().isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Interview time must be in the future.");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
