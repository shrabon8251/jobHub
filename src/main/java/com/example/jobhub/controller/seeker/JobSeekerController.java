package com.example.jobhub.controller.seeker;

import com.example.jobhub.dto.form.ApplicationForm;
import com.example.jobhub.dto.form.EducationForm;
import com.example.jobhub.dto.form.ExperienceForm;
import com.example.jobhub.dto.form.ProfileForm;
import com.example.jobhub.dto.form.SkillForm;
import com.example.jobhub.entity.Education;
import com.example.jobhub.entity.Experience;
import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.entity.embeddable.Address;
import com.example.jobhub.service.JobApplicationService;
import com.example.jobhub.service.JobSeekerService;
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
@RequestMapping("/seeker")
public class JobSeekerController {

    private final JobSeekerService seekerService;
    private final JobApplicationService applicationService;

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("profile", seekerService.profile());
        model.addAttribute("skillForm", new SkillForm());
        return "seeker/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        model.addAttribute("profileForm", toProfileForm(seekerService.profile()));
        return "seeker/profile-form";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute ProfileForm form,
                                BindingResult bindingResult,
                                RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            return "seeker/profile-form";
        }
        try {
            seekerService.updateProfile(form);
            flash.addFlashAttribute("success", "Profile updated.");
            return "redirect:/seeker/profile";
        } catch (IllegalArgumentException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
            return "redirect:/seeker/profile/edit";
        }
    }

    @PostMapping("/profile/photo/delete")
    public String deleteProfilePhoto(RedirectAttributes flash) {
        seekerService.deleteProfilePhoto();
        flash.addFlashAttribute("success", "Profile photo removed.");
        return "redirect:/seeker/profile/edit";
    }

    @GetMapping("/education/new")
    public String newEducation(Model model) {
        model.addAttribute("educationForm", new EducationForm());
        return "seeker/education-form";
    }

    @PostMapping("/education")
    public String addEducation(@Valid @ModelAttribute EducationForm form,
                               BindingResult bindingResult,
                               RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            return "seeker/education-form";
        }
        try {
            seekerService.addEducation(form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("education", exception.getMessage());
            return "seeker/education-form";
        }
        flash.addFlashAttribute("success", "Education added.");
        return "redirect:/seeker/profile";
    }

    @GetMapping("/education/{id}/edit")
    public String editEducation(@PathVariable Long id, Model model) {
        model.addAttribute("educationForm", toEducationForm(seekerService.education(id)));
        model.addAttribute("educationId", id);
        return "seeker/education-form";
    }

    @PostMapping("/education/{id}")
    public String updateEducation(@PathVariable Long id,
                                  @Valid @ModelAttribute EducationForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("educationId", id);
            return "seeker/education-form";
        }
        try {
            seekerService.updateEducation(id, form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("education", exception.getMessage());
            model.addAttribute("educationId", id);
            return "seeker/education-form";
        }
        flash.addFlashAttribute("success", "Education updated.");
        return "redirect:/seeker/profile";
    }

    @PostMapping("/education/{id}/delete")
    public String deleteEducation(@PathVariable Long id, RedirectAttributes flash) {
        seekerService.deleteEducation(id);
        flash.addFlashAttribute("success", "Education deleted.");
        return "redirect:/seeker/profile";
    }

    @GetMapping("/experience/new")
    public String newExperience(Model model) {
        model.addAttribute("experienceForm", new ExperienceForm());
        return "seeker/experience-form";
    }

    @PostMapping("/experience")
    public String addExperience(@Valid @ModelAttribute("experienceForm") ExperienceForm form,
                                BindingResult bindingResult,
                                RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            return "seeker/experience-form";
        }
        try {
            seekerService.addExperience(form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("experience", exception.getMessage());
            return "seeker/experience-form";
        }
        flash.addFlashAttribute("success", "Experience added.");
        return "redirect:/seeker/profile";
    }

    @GetMapping("/experience/{id}/edit")
    public String editExperience(@PathVariable Long id, Model model) {
        model.addAttribute("experienceForm", toExperienceForm(seekerService.experience(id)));
        model.addAttribute("experienceId", id);
        return "seeker/experience-form";
    }

    @PostMapping("/experience/{id}")
    public String updateExperience(@PathVariable Long id,
                                   @Valid @ModelAttribute("experienceForm") ExperienceForm form,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("experienceId", id);
            return "seeker/experience-form";
        }
        try {
            seekerService.updateExperience(id, form);
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("experience", exception.getMessage());
            model.addAttribute("experienceId", id);
            return "seeker/experience-form";
        }
        flash.addFlashAttribute("success", "Experience updated.");
        return "redirect:/seeker/profile";
    }

    @PostMapping("/experience/{id}/delete")
    public String deleteExperience(@PathVariable Long id, RedirectAttributes flash) {
        seekerService.deleteExperience(id);
        flash.addFlashAttribute("success", "Experience deleted.");
        return "redirect:/seeker/profile";
    }

    @PostMapping("/skills")
    public String addSkill(@Valid @ModelAttribute SkillForm form,
                           BindingResult bindingResult,
                           RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().stream()
                    .findFirst()
                    .ifPresent(error -> flash.addFlashAttribute("error", error.getDefaultMessage()));
            return "redirect:/seeker/profile";
        }
        try {
            seekerService.addSkill(form);
        } catch (IllegalArgumentException exception) {
            flash.addFlashAttribute("error", exception.getMessage());
            return "redirect:/seeker/profile";
        }
        flash.addFlashAttribute("success", "Skill added.");
        return "redirect:/seeker/profile";
    }

    @PostMapping("/skills/{id}/delete")
    public String removeSkill(@PathVariable Long id, RedirectAttributes flash) {
        seekerService.removeSkill(id);
        flash.addFlashAttribute("success", "Skill removed.");
        return "redirect:/seeker/profile";
    }

    @GetMapping("/saved-jobs")
    public String saved(Model model) {
        model.addAttribute("savedJobs", applicationService.saved());
        return "seeker/saved-jobs";
    }

    @PostMapping("/saved-jobs/{jobId}")
    public String save(@PathVariable Long jobId, RedirectAttributes flash) {
        applicationService.save(jobId);
        flash.addFlashAttribute("success", "Job saved.");
        return "redirect:/jobs/" + jobId;
    }

    @PostMapping("/saved-jobs/{id}/delete")
    public String removeSaved(@PathVariable Long id, RedirectAttributes flash) {
        applicationService.removeSaved(id);
        flash.addFlashAttribute("success", "Saved job removed.");
        return "redirect:/seeker/saved-jobs";
    }

    @GetMapping("/applications")
    public String listApplications(Model model) {
        model.addAttribute("applications", applicationService.applications());
        return "seeker/applications";
    }

    @GetMapping("/applications/{id}")
    public String application(@PathVariable Long id, Model model) {
        model.addAttribute("applicationRecord", applicationService.application(id));
        return "seeker/application-detail";
    }

    @GetMapping("/applications/job/{jobId}/apply")
    public String applyForm(@PathVariable Long jobId, Model model) {
        model.addAttribute("jobId", jobId);
        model.addAttribute("applicationForm", new ApplicationForm());
        return "seeker/application-form";
    }

    @PostMapping("/applications/job/{jobId}")
    public String apply(@PathVariable Long jobId,
                        @Valid @ModelAttribute("applicationForm") ApplicationForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes flash) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("jobId", jobId);
            return "seeker/application-form";
        }
        var application = applicationService.apply(jobId, form);
        flash.addFlashAttribute("success", "Application submitted.");
        return "redirect:/seeker/applications/" + application.getId();
    }

    @GetMapping("/interviews/{id}")
    public String interview(@PathVariable Long id, Model model) {
        model.addAttribute("interview", applicationService.interview(id));
        return "seeker/interview-detail";
    }

    private ProfileForm toProfileForm(JobSeekerProfile profile) {
        ProfileForm form = new ProfileForm();
        form.setFullName(profile.getFullName());
        form.setPhone(profile.getPhone());
        form.setBio(profile.getBio());
        form.setLinkedinUrl(profile.getLinkedinUrl());
        form.setGithubUrl(profile.getGithubUrl());
        form.setPortfolioUrl(profile.getPortfolioUrl());
        form.setWhatsappNumber(profile.getWhatsappNumber());
        copyAddress(profile.getAddress(), form);
        return form;
    }

    private void copyAddress(Address address, ProfileForm form) {
        if (address == null) {
            return;
        }
        form.setStreet(address.getStreet());
        form.setCity(address.getCity());
        form.setCountry(address.getCountry());
        form.setPostalCode(address.getPostalCode());
    }

    private EducationForm toEducationForm(Education education) {
        EducationForm form = new EducationForm();
        form.setDegree(education.getDegree());
        form.setInstitution(education.getInstitution());
        form.setFieldOfStudy(education.getFieldOfStudy());
        form.setStartYear(education.getStartYear());
        form.setEndYear(education.getEndYear());
        return form;
    }

    private ExperienceForm toExperienceForm(Experience experience) {
        ExperienceForm form = new ExperienceForm();
        form.setCompany(experience.getCompany());
        form.setPosition(experience.getPosition());
        form.setDescription(experience.getDescription());
        form.setStartDate(experience.getStartDate());
        form.setEndDate(experience.getEndDate());
        return form;
    }
}
