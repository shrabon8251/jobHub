package com.example.jobhub.controller;

import com.example.jobhub.entity.Application;
import com.example.jobhub.service.CurrentUserService;
import com.example.jobhub.service.JobApplicationService;
import com.example.jobhub.service.JobSeekerService;
import com.example.jobhub.service.RecruiterService;
import com.example.jobhub.service.RecruitmentWorkflowService;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import com.example.jobhub.entity.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CurrentUserService current;
    private final JobApplicationService seekerApplications;
    private final JobSeekerService seekers;
    private final RecruiterService recruiter;
    private final RecruitmentWorkflowService workflow;

    @GetMapping("/seeker/dashboard")
    public String seeker(Model model) {
        model.addAttribute("user", current.requireCurrentUser());
        model.addAttribute("profile", seekers.profile());
        model.addAttribute("savedCount", seekerApplications.saved().size());
        List<Application> applications = seekerApplications.applications();
        model.addAttribute("applications", applications);
        model.addAttribute("interviewCount", countInterviews(applications));
        return "seeker/dashboard";
    }

    @GetMapping("/recruiter/dashboard")
    public String recruiter(Model model) {
        model.addAttribute("user", current.requireCurrentUser());
        model.addAttribute("profile", recruiter.profile());
        model.addAttribute("jobs", recruiter.jobs(null, null, 0));
        List<Application> applications = workflow.applications();
        model.addAttribute("applications", applications);
        model.addAttribute("interviewCount", countInterviews(applications));
        model.addAttribute("selectedCount", countByStatus(applications, ApplicationStatus.SELECTED));
        model.addAttribute("applicationCounts", countApplicationsByStatus(applications));
        return "recruiter/dashboard";
    }

    private long countInterviews(List<Application> applications) {
        return applications.stream()
                .filter(application -> application.getInterview() != null)
                .count();
    }

    private long countByStatus(List<Application> applications, ApplicationStatus status) {
        return applications.stream()
                .filter(application -> application.getStatus() == status)
                .count();
    }

    private Map<ApplicationStatus, Long> countApplicationsByStatus(List<Application> applications) {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            counts.put(status, countByStatus(applications, status));
        }
        return counts;
    }
}
