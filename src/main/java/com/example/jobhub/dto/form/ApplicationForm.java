package com.example.jobhub.dto.form;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationForm {

    @Size(max = 5000, message = "Cover letter must be 5,000 characters or fewer.")
    private String coverLetter;
}
