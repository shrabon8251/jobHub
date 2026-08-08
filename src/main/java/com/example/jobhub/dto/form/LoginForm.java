package com.example.jobhub.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotBlank(message = "Email address is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 150, message = "Email address must be 150 characters or fewer.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(max = 72, message = "Password must be 72 characters or fewer.")
    private String password;
}
