package com.example.jobhub.dto.form;

import com.example.jobhub.validation.ValidExperienceDates;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidExperienceDates
public class ExperienceForm {

    @NotBlank(message = "Company is required.")
    @Size(max = 150, message = "Company must be 150 characters or fewer.")
    private String company;

    @NotBlank(message = "Position is required.")
    @Size(max = 150, message = "Position must be 150 characters or fewer.")
    private String position;

    @Size(max = 4000, message = "Description must be 4,000 characters or fewer.")
    private String description;

    @NotNull(message = "Start date is required.")
    @PastOrPresent(message = "Start date cannot be in the future.")
    private LocalDate startDate;

    @PastOrPresent(message = "End date cannot be in the future.")
    private LocalDate endDate;
}
