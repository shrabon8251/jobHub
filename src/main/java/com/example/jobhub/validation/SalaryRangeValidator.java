package com.example.jobhub.validation;

import com.example.jobhub.dto.form.JobForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SalaryRangeValidator implements ConstraintValidator<ValidSalaryRange, JobForm> {

    @Override
    public boolean isValid(JobForm form, ConstraintValidatorContext context) {
        if (form == null || form.getSalaryMin() == null || form.getSalaryMax() == null) {
            return true;
        }
        return form.getSalaryMin().compareTo(form.getSalaryMax()) <= 0;
    }
}
