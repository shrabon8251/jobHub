package com.example.jobhub.controller.admin;

import com.example.jobhub.dto.form.CategoryForm;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.service.AdminService;
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
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.statistics());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "") String q,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) Boolean enabled,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        model.addAttribute("users", adminService.users(q, role, enabled, page));
        model.addAttribute("roles", new Role[]{Role.JOB_SEEKER, Role.RECRUITER, Role.ADMIN});
        model.addAttribute("q", q);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedEnabled", enabled);
        return "admin/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enable(@PathVariable Long id, RedirectAttributes flash) {
        try {
            return redirectAfterAction(flash, "User account enabled.", () -> adminService.setEnabled(id, true),
                    "redirect:/admin/users");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{id}/disable")
    public String disable(@PathVariable Long id, RedirectAttributes flash) {
        try {
            return redirectAfterAction(flash, "User account disabled.", () -> adminService.setEnabled(id, false),
                    "redirect:/admin/users");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/users";
        }
    }

    @GetMapping("/jobs")
    public String jobs(@RequestParam(defaultValue = "") String q,
                       @RequestParam(required = false) JobStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("jobs", adminService.jobs(q, status, page));
        model.addAttribute("statuses", JobStatus.values());
        model.addAttribute("q", q);
        model.addAttribute("selectedStatus", status);
        return "admin/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String job(@PathVariable Long id, Model model) {
        model.addAttribute("job", adminService.job(id));
        return "admin/job-detail";
    }

    @PostMapping("/jobs/{id}/suspend")
    public String suspend(@PathVariable Long id, RedirectAttributes flash) {
        return moderate(id, flash, "suspended", () -> adminService.suspend(id));
    }

    @PostMapping("/jobs/{id}/remove")
    public String remove(@PathVariable Long id, RedirectAttributes flash) {
        return moderate(id, flash, "removed", () -> adminService.remove(id));
    }

    @PostMapping("/jobs/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes flash) {
        return moderate(id, flash, "restored", () -> adminService.restore(id));
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", adminService.categories());
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
        return "admin/categories";
    }

    @PostMapping("/categories")
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categories", adminService.categories());
            return "admin/categories";
        }
        try {
            adminService.createCategory(form);
            flash.addFlashAttribute("success", "Category created.");
            return "redirect:/admin/categories";
        } catch (IllegalArgumentException ex) {
            result.reject("category", ex.getMessage());
            model.addAttribute("categories", adminService.categories());
            return "admin/categories";
        }
    }

    @PostMapping("/categories/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("categoryForm") CategoryForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categories", adminService.categories());
            return "admin/categories";
        }
        try {
            adminService.updateCategory(id, form);
            flash.addFlashAttribute("success", "Category updated.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        try {
            adminService.deleteCategory(id);
            flash.addFlashAttribute("success", "Category deleted.");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    private String moderate(Long id, RedirectAttributes flash, String action, Runnable operation) {
        try {
            operation.run();
            flash.addFlashAttribute("success", "Job " + action + ".");
        } catch (IllegalArgumentException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/jobs/" + id;
    }

    private String redirectAfterAction(RedirectAttributes flash, String successMessage,
                                       Runnable operation, String redirect) {
        operation.run();
        flash.addFlashAttribute("success", successMessage);
        return redirect;
    }
}
