package com.example.jobhub.controller.recruiter;

import com.example.jobhub.dto.form.JobForm;
import com.example.jobhub.dto.form.RecruiterProfileForm;
import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.RecruiterProfile;
import com.example.jobhub.entity.embeddable.Address;
import com.example.jobhub.entity.enums.EmploymentType;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.repository.CategoryRepository;
import com.example.jobhub.service.RecruiterService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recruiter")
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("profile", recruiterService.profile());
        return "recruiter/profile";
    }

    @GetMapping("/profile/edit")
    public String profileEdit(Model model) {
        model.addAttribute("recruiterProfileForm", toProfileForm(recruiterService.profile()));
        return "recruiter/profile-form";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute RecruiterProfileForm form,
                                BindingResult bindingResult,
                                RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            return "recruiter/profile-form";
        }
        try {
            recruiterService.updateProfile(form);
            flash.addFlashAttribute("success", "Company profile updated.");
            return "redirect:/recruiter/profile";
        } catch (IllegalArgumentException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
            return "redirect:/recruiter/profile/edit";
        }
    }

    @PostMapping("/profile/logo/delete")
    public String deleteLogo(RedirectAttributes flash) {
        recruiterService.deleteCompanyLogo();
        flash.addFlashAttribute("success", "Company logo removed.");
        return "redirect:/recruiter/profile/edit";
    }

    @PostMapping("/profile/cover/delete")
    public String deleteCover(RedirectAttributes flash) {
        recruiterService.deleteCompanyCover();
        flash.addFlashAttribute("success", "Company cover removed.");
        return "redirect:/recruiter/profile/edit";
    }

    @GetMapping("/jobs")
    public String jobs(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) JobStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("jobs", recruiterService.jobs(keyword, status, page));
        model.addAttribute("statuses", JobStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "recruiter/jobs";
    }

    @GetMapping("/jobs/new")
    public String newJob(Model model) {
        addJobFormOptions(model);
        model.addAttribute("jobForm", new JobForm());
        return "recruiter/job-form";
    }

    @PostMapping("/jobs")
    public String create(@Valid @ModelAttribute JobForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            addJobFormOptions(model);
            return "recruiter/job-form";
        }
        Job job;
        try {
            job = recruiterService.create(form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("job", exception.getMessage());
            addJobFormOptions(model);
            return "recruiter/job-form";
        }
        flash.addFlashAttribute("success", "Job created and published.");
        return "redirect:/recruiter/jobs/" + job.getId();
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("job", recruiterService.job(id));
        return "recruiter/job-detail";
    }

    @GetMapping("/jobs/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("jobForm", toJobForm(recruiterService.job(id)));
        model.addAttribute("jobId", id);
        addJobFormOptions(model);
        return "recruiter/job-form";
    }

    @PostMapping("/jobs/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute JobForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            addJobFormOptions(model);
            model.addAttribute("jobId", id);
            return "recruiter/job-form";
        }
        try {
            recruiterService.update(id, form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("job", exception.getMessage());
            addJobFormOptions(model);
            model.addAttribute("jobId", id);
            return "recruiter/job-form";
        }
        flash.addFlashAttribute("success", "Job updated.");
        return "redirect:/recruiter/jobs/" + id;
    }

    @PostMapping("/jobs/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        recruiterService.delete(id);
        flash.addFlashAttribute("success", "Job deleted.");
        return "redirect:/recruiter/jobs";
    }

    @PostMapping("/jobs/{id}/image/delete")
    public String deleteJobImage(@PathVariable Long id, RedirectAttributes flash) {
        recruiterService.deleteJobImage(id);
        flash.addFlashAttribute("success", "Job image removed.");
        return "redirect:/recruiter/jobs/" + id;
    }

    @PostMapping("/jobs/{id}/pause")
    public String pause(@PathVariable Long id, RedirectAttributes flash) {
        return completeJobAction(id, flash, "Job paused.", recruiterService::pause);
    }

    @PostMapping("/jobs/{id}/resume")
    public String resume(@PathVariable Long id, RedirectAttributes flash) {
        return completeJobAction(id, flash, "Job resumed.", recruiterService::resume);
    }

    @PostMapping("/jobs/{id}/close")
    public String close(@PathVariable Long id, RedirectAttributes flash) {
        return completeJobAction(id, flash, "Job closed.", recruiterService::close);
    }

    private String completeJobAction(Long id, RedirectAttributes flash,
                                     String successMessage,
                                     java.util.function.Consumer<Long> action) {
        action.accept(id);
        flash.addFlashAttribute("success", successMessage);
        return "redirect:/recruiter/jobs/" + id;
    }

    private RecruiterProfileForm toProfileForm(RecruiterProfile profile) {
        RecruiterProfileForm form = new RecruiterProfileForm();
        form.setCompanyName(profile.getCompanyName());
        form.setCompanyDescription(profile.getCompanyDescription());
        form.setWebsite(profile.getWebsite());
        form.setPhone(profile.getPhone());
        copyAddress(profile.getAddress(), form);
        return form;
    }

    private void copyAddress(Address address, RecruiterProfileForm form) {
        if (address == null) {
            return;
        }
        form.setStreet(address.getStreet());
        form.setCity(address.getCity());
        form.setCountry(address.getCountry());
        form.setPostalCode(address.getPostalCode());
    }

    private JobForm toJobForm(Job job) {
        JobForm form = new JobForm();
        form.setTitle(job.getTitle());
        form.setDescription(job.getDescription());
        form.setRequirements(job.getRequirements());
        form.setResponsibilities(job.getResponsibilities());
        form.setSalaryMin(job.getSalaryMin());
        form.setSalaryMax(job.getSalaryMax());
        form.setLocation(job.getLocation());
        form.setEmploymentType(job.getEmploymentType());
        form.setDeadline(job.getDeadline());
        form.setCategoryId(job.getCategory().getId());
        return form;
    }

    private void addJobFormOptions(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("employmentTypes", EmploymentType.values());
    }
}
