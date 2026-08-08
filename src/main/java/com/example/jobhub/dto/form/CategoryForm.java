package com.example.jobhub.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryForm {

    @NotBlank(message = "Category name is required.")
    @Size(max = 100, message = "Category name must be 100 characters or fewer.")
    private String name;
}
