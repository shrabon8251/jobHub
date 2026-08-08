package com.example.jobhub.service;

import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.enums.EmploymentType;
import com.example.jobhub.entity.enums.JobStatus;
import com.example.jobhub.exception.ResourceNotFoundException;
import com.example.jobhub.repository.JobRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobDiscoveryService {

    private static final int SEARCH_PAGE_SIZE = 10;

    private final JobRepository jobs;

    public Page<Job> search(String keyword, String location, Long categoryId,
                            EmploymentType type, BigDecimal salaryMin,
                            BigDecimal salaryMax, String sort, int page) {
        return jobs.searchPublicJobs(
                JobStatus.ACTIVE,
                LocalDate.now(),
                normalize(keyword),
                normalize(location),
                categoryId,
                type,
                salaryMin,
                salaryMax,
                PageRequest.of(Math.max(page, 0), SEARCH_PAGE_SIZE, sortOrder(sort)));
    }

    public Job publicJob(Long id) {
        return jobs.findByIdAndStatusAndDeadlineGreaterThanEqual(id, JobStatus.ACTIVE, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Job was not found."));
    }

    private Sort sortOrder(String sort) {
        return switch (sort == null ? "newest" : sort) {
            case "deadline" -> Sort.by("deadline").ascending();
            case "salary" -> Sort.by("salaryMax").descending();
            default -> Sort.by("createdAt").descending();
        };
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
