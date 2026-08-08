package com.example.jobhub.dto.form;

import com.example.jobhub.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationForm {

    @NotBlank(message = "Name is required.")
    @Size(max = 100, message = "Name must be 100 characters or fewer.")
    private String name;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 150, message = "Email address must be 150 characters or fewer.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s]).{8,72}$",
            message = "Password must include a letter, a number, and a symbol.")
    private String password;

    @NotNull(message = "Choose whether you are joining as a job seeker or recruiter.")
    private Role role;

    @NotBlank(message = "Profile name is required.")
    @Size(max = 150, message = "Profile name must be 150 characters or fewer.")
    private String profileName;
}
