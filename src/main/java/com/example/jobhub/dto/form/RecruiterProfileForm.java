package com.example.jobhub.dto.form;

import com.example.jobhub.validation.ValidImageUpload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class RecruiterProfileForm {

    @ValidImageUpload
    private MultipartFile companyLogo;

    @ValidImageUpload
    private MultipartFile companyCover;

    @NotBlank(message = "Company name is required.")
    @Size(max = 150, message = "Company name must be 150 characters or fewer.")
    private String companyName;

    @Size(max = 4000, message = "Company description must be 4,000 characters or fewer.")
    private String companyDescription;

    @Pattern(regexp = "^$|https?://[^\\s]+$", message = "Enter a valid http(s) URL.")
    @Size(max = 255, message = "Website URL must be 255 characters or fewer.")
    private String website;

    @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,50}$", message = "Enter a valid phone number.")
    private String phone;

    @Size(max = 150, message = "Street must be 150 characters or fewer.")
    private String street;

    @Size(max = 100, message = "City must be 100 characters or fewer.")
    private String city;

    @Size(max = 100, message = "Country must be 100 characters or fewer.")
    private String country;

    @Size(max = 30, message = "Postal code must be 30 characters or fewer.")
    private String postalCode;
}
