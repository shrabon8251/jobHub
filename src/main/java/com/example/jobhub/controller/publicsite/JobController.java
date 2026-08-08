package com.example.jobhub.controller.publicsite;

import com.example.jobhub.entity.enums.EmploymentType;
import com.example.jobhub.repository.CategoryRepository;
import com.example.jobhub.service.JobDiscoveryService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class JobController {

    private final JobDiscoveryService jobDiscoveryService;
    private final CategoryRepository categoryRepository;

    @GetMapping({"/", "/jobs"})
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String location,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) EmploymentType employmentType,
                       @RequestParam(required = false) BigDecimal salaryMin,
                       @RequestParam(required = false) BigDecimal salaryMax,
                       @RequestParam(defaultValue = "newest") String sort,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("jobs", jobDiscoveryService.search(
                keyword, location, categoryId, employmentType, salaryMin, salaryMax, sort, page));
        addFilterOptions(model);
        addFilterValues(model, keyword, location, categoryId, employmentType, salaryMin, salaryMax, sort);
        return "publicsite/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobDiscoveryService.publicJob(id));
        return "publicsite/job-detail";
    }

    private void addFilterOptions(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("types", EmploymentType.values());
    }

    private void addFilterValues(Model model, String keyword, String location, Long categoryId,
                                 EmploymentType employmentType, BigDecimal salaryMin,
                                 BigDecimal salaryMax, String sort) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("employmentType", employmentType);
        model.addAttribute("salaryMin", salaryMin);
        model.addAttribute("salaryMax", salaryMax);
        model.addAttribute("sort", sort);
    }
}
