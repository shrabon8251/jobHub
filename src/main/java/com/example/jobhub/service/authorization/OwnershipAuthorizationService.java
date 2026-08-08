package com.example.jobhub.service.authorization;

import com.example.jobhub.entity.Application;
import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.entity.Education;
import com.example.jobhub.entity.Experience;
import com.example.jobhub.entity.Interview;
import com.example.jobhub.entity.SavedJob;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Server-side ownership policy used by future job, applicant, and profile services.
 * Route role checks do not replace these record-level checks.
 */
@Component("ownershipAuthorization")
public class OwnershipAuthorizationService {

    public boolean canManageJob(Authentication authentication, Job job) {
        return hasAuthority(authentication, "ROLE_RECRUITER")
                && job != null
                && job.getRecruiter() != null
                && job.getRecruiter().getUser() != null
                && isCurrentUser(authentication, job.getRecruiter().getUser().getEmail());
    }

    public boolean canManageApplication(Authentication authentication, Application application) {
        return application != null && canManageJob(authentication, application.getJob());
    }

    public boolean canAccessJobSeekerProfile(Authentication authentication, JobSeekerProfile profile) {
        return hasAuthority(authentication, "ROLE_JOB_SEEKER")
                && profile != null
                && profile.getUser() != null
                && isCurrentUser(authentication, profile.getUser().getEmail());
    }
    public boolean canManageEducation(Authentication authentication, Education education) {
        return education != null && canAccessJobSeekerProfile(authentication, education.getJobSeeker());
    }

    public boolean canManageExperience(Authentication authentication, Experience experience) {
        return experience != null && canAccessJobSeekerProfile(authentication, experience.getJobSeeker());
    }

    public boolean canManageSavedJob(Authentication authentication, SavedJob savedJob) {
        return savedJob != null && canAccessJobSeekerProfile(authentication, savedJob.getJobSeeker());
    }

    public boolean canAccessInterview(Authentication authentication, Interview interview) {
        return interview != null
                && interview.getApplication() != null
                && canAccessJobSeekerProfile(authentication, interview.getApplication().getJobSeeker());
    }

    private boolean isCurrentUser(Authentication authentication, String email) {
        return authentication != null && Objects.equals(authentication.getName(), email);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }
}
