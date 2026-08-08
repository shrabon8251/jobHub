package com.example.jobhub.controller.auth;

import com.example.jobhub.dto.form.RegistrationForm;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.exception.DuplicateEmailException;
import com.example.jobhub.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping("/register")
    public String form(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            RegistrationForm form = new RegistrationForm();
            form.setRole(Role.JOB_SEEKER);
            model.addAttribute("registrationForm", form);
        }
        addRoles(model);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationForm form,
                           BindingResult bindingResult,
                           RedirectAttributes flash,
                           Model model) {
        registerIfValid(form, bindingResult);
        if (bindingResult.hasErrors()) {
            addRoles(model);
            return "auth/register";
        }
        flash.addFlashAttribute("success", "Registration complete. Please sign in.");
        return "redirect:/login";
    }

    private void registerIfValid(RegistrationForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return;
        }
        try {
            registrationService.register(form);
        } catch (DuplicateEmailException exception) {
            bindingResult.rejectValue("email", "duplicate", exception.getMessage());
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("registration", exception.getMessage());
        }
    }

    private void addRoles(Model model) {
        model.addAttribute("roles", new Role[]{Role.JOB_SEEKER, Role.RECRUITER});
    }
}
