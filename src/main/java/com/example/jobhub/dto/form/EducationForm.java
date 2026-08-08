package com.example.jobhub.dto.form;

import com.example.jobhub.validation.ValidEducationYears;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidEducationYears
public class EducationForm {

    @NotBlank(message = "Degree or qualification is required.")
    @Size(max = 150, message = "Degree or qualification must be 150 characters or fewer.")
    private String degree;

    @NotBlank(message = "Institution is required.")
    @Size(max = 200, message = "Institution must be 200 characters or fewer.")
    private String institution;

    @Size(max = 150, message = "Field of study must be 150 characters or fewer.")
    private String fieldOfStudy;

    @NotNull(message = "Start year is required.")
    @Min(value = 1900, message = "Start year must be 1900 or later.")
    @Max(value = 2100, message = "Start year must be 2100 or earlier.")
    private Integer startYear;

    @Min(value = 1900, message = "End year must be 1900 or later.")
    @Max(value = 2100, message = "End year must be 2100 or earlier.")
    private Integer endYear;
}
