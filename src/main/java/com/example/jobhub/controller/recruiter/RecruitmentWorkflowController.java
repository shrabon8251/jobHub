package com.example.jobhub.controller.recruiter;

import com.example.jobhub.dto.form.InterviewForm;
import com.example.jobhub.entity.Interview;
import com.example.jobhub.service.RecruitmentWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recruiter")
public class RecruitmentWorkflowController {

    private final RecruitmentWorkflowService workflow;

    @GetMapping("/applications")
    public String applications(Model model) {
        model.addAttribute("applications", workflow.applications());
        return "recruiter/applications";
    }

    @GetMapping("/applications/{id}")
    public String application(@PathVariable Long id, Model model) {
        model.addAttribute("applicationRecord", workflow.application(id));
        return "recruiter/application-detail";
    }

    @PostMapping("/applications/{id}/review")
    public String review(@PathVariable Long id, RedirectAttributes flash) {
        workflow.review(id);
        return redirectWithSuccess(id, flash, "Application moved to reviewing.");
    }

    @PostMapping("/applications/{id}/shortlist")
    public String shortlist(@PathVariable Long id, RedirectAttributes flash) {
        workflow.shortlist(id);
        return redirectWithSuccess(id, flash, "Candidate shortlisted.");
    }

    @PostMapping("/applications/{id}/select")
    public String select(@PathVariable Long id, RedirectAttributes flash) {
        workflow.select(id);
        return redirectWithSuccess(id, flash, "Candidate selected.");
    }

    @PostMapping("/applications/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes flash) {
        workflow.reject(id);
        return redirectWithSuccess(id, flash, "Application rejected.");
    }

    @GetMapping("/applications/{id}/interview")
    public String scheduleForm(@PathVariable Long id, Model model) {
        workflow.application(id);
        model.addAttribute("applicationId", id);
        model.addAttribute("interviewForm", new InterviewForm());
        return "recruiter/interview-form";
    }

    @PostMapping("/applications/{id}/interview")
    public String schedule(@PathVariable Long id,
                           @Valid @ModelAttribute InterviewForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("applicationId", id);
            return "recruiter/interview-form";
        }
        Interview interview;
        try {
            interview = workflow.schedule(id, form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("interview", exception.getMessage());
            model.addAttribute("applicationId", id);
            return "recruiter/interview-form";
        }
        flash.addFlashAttribute("success", "Interview scheduled.");
        return "redirect:/recruiter/interviews/" + interview.getId();
    }

    @GetMapping("/interviews/{id}")
    public String interview(@PathVariable Long id, Model model) {
        model.addAttribute("interview", workflow.interview(id));
        return "recruiter/interview-detail";
    }

    @GetMapping("/interviews/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("interviewId", id);
        model.addAttribute("interviewForm", toForm(workflow.interview(id)));
        return "recruiter/interview-form";
    }

    @PostMapping("/interviews/{id}")
    public String reschedule(@PathVariable Long id,
                             @Valid @ModelAttribute InterviewForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("interviewId", id);
            return "recruiter/interview-form";
        }
        try {
            workflow.reschedule(id, form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("interview", exception.getMessage());
            model.addAttribute("interviewId", id);
            return "recruiter/interview-form";
        }
        flash.addFlashAttribute("success", "Interview updated.");
        return "redirect:/recruiter/interviews/" + id;
    }

    @PostMapping("/interviews/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes flash) {
        workflow.cancel(id);
        flash.addFlashAttribute("success", "Interview cancelled.");
        return "redirect:/recruiter/applications";
    }

    private String redirectWithSuccess(Long id, RedirectAttributes flash, String message) {
        flash.addFlashAttribute("success", message);
        return "redirect:/recruiter/applications/" + id;
    }

    private InterviewForm toForm(Interview interview) {
        InterviewForm form = new InterviewForm();
        form.setInterviewDate(interview.getInterviewDate());
        form.setInterviewTime(interview.getInterviewTime());
        form.setMeetingLink(interview.getMeetingLink());
        form.setNotes(interview.getNotes());
        return form;
    }
}
