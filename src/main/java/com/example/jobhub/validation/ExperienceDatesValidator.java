package com.example.jobhub.validation;

import com.example.jobhub.dto.form.ExperienceForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ExperienceDatesValidator implements ConstraintValidator<ValidExperienceDates, ExperienceForm> {

    @Override
    public boolean isValid(ExperienceForm form, ConstraintValidatorContext context) {
        if (form == null || form.getStartDate() == null) {
            return true;
        }
        if (form.getStartDate().isAfter(LocalDate.now())) {
            return violation(context, "Start date cannot be in the future.");
        }
        return form.getEndDate() == null || !form.getEndDate().isBefore(form.getStartDate())
                || violation(context, "End date must not be before start date.");
    }

    private boolean violation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("startDate")
                .addConstraintViolation();
        return false;
    }
}
