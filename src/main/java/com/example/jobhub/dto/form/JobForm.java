package com.example.jobhub.dto.form;

import com.example.jobhub.entity.enums.EmploymentType;
import com.example.jobhub.validation.ValidImageUpload;
import com.example.jobhub.validation.ValidSalaryRange;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ValidSalaryRange
public class JobForm {

    @ValidImageUpload
    private MultipartFile jobImage;

    @NotBlank(message = "Job title is required.")
    @Size(max = 200, message = "Job title must be 200 characters or fewer.")
    private String title;

    @NotBlank(message = "Job description is required.")
    @Size(max = 10000, message = "Job description must be 10,000 characters or fewer.")
    private String description;

    @Size(max = 10000, message = "Requirements must be 10,000 characters or fewer.")
    private String requirements;

    @Size(max = 10000, message = "Responsibilities must be 10,000 characters or fewer.")
    private String responsibilities;

    @DecimalMin(value = "0.00", message = "Minimum salary cannot be negative.")
    @Digits(integer = 10, fraction = 2, message = "Minimum salary must be a valid amount.")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.00", message = "Maximum salary cannot be negative.")
    @Digits(integer = 10, fraction = 2, message = "Maximum salary must be a valid amount.")
    private BigDecimal salaryMax;

    @NotBlank(message = "Location is required.")
    @Size(max = 150, message = "Location must be 150 characters or fewer.")
    private String location;

    @NotNull(message = "Choose an employment type.")
    private EmploymentType employmentType;

    @NotNull(message = "Application deadline is required.")
    @Future(message = "Application deadline must be in the future.")
    private LocalDate deadline;

    @NotNull(message = "Choose a category.")
    @Positive(message = "Choose a valid category.")
    private Long categoryId;
}
