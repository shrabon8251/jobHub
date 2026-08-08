package com.example.jobhub.validation;

import com.example.jobhub.dto.form.EducationForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

public class EducationYearsValidator implements ConstraintValidator<ValidEducationYears, EducationForm> {

    @Override
    public boolean isValid(EducationForm form, ConstraintValidatorContext context) {
        if (form == null || form.getStartYear() == null) {
            return true;
        }
        if (form.getStartYear() > Year.now().getValue() + 1) {
            return violation(context, "Start year cannot be in the future.");
        }
        return form.getEndYear() == null || form.getEndYear() >= form.getStartYear()
                || violation(context, "End year must not be before start year.");
    }

    private boolean violation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("startYear")
                .addConstraintViolation();
        return false;
    }
}
