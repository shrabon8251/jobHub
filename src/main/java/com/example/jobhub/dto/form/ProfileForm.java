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
public class ProfileForm {

    @ValidImageUpload
    private MultipartFile profileImage;

    @NotBlank(message = "Full name is required.")
    @Size(max = 150, message = "Full name must be 150 characters or fewer.")
    private String fullName;

    @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,50}$", message = "Enter a valid phone number.")
    private String phone;

    @Size(max = 4000, message = "Bio must be 4,000 characters or fewer.")
    private String bio;

    @Pattern(regexp = "^$|https?://[^\\s]+$", message = "Enter a valid http(s) URL.")
    @Size(max = 255, message = "LinkedIn URL must be 255 characters or fewer.")
    private String linkedinUrl;

    @Pattern(regexp = "^$|https?://[^\\s]+$", message = "Enter a valid http(s) URL.")
    @Size(max = 255, message = "GitHub URL must be 255 characters or fewer.")
    private String githubUrl;

    @Pattern(regexp = "^$|https?://[^\\s]+$", message = "Enter a valid http(s) URL.")
    @Size(max = 255, message = "Portfolio URL must be 255 characters or fewer.")
    private String portfolioUrl;

    @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,50}$", message = "Enter a valid WhatsApp number.")
    private String whatsappNumber;

    @Size(max = 150, message = "Street must be 150 characters or fewer.")
    private String street;

    @Size(max = 100, message = "City must be 100 characters or fewer.")
    private String city;

    @Size(max = 100, message = "Country must be 100 characters or fewer.")
    private String country;

    @Size(max = 30, message = "Postal code must be 30 characters or fewer.")
    private String postalCode;
}
