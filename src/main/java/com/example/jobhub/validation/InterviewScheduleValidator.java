package com.example.jobhub.validation;

import com.example.jobhub.dto.form.InterviewForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class InterviewScheduleValidator implements ConstraintValidator<ValidInterviewSchedule, InterviewForm> {

    @Override
    public boolean isValid(InterviewForm form, ConstraintValidatorContext context) {
        if (form == null || form.getInterviewDate() == null || form.getInterviewTime() == null) {
            return true;
        }
        if (!LocalDateTime.of(form.getInterviewDate(), form.getInterviewTime()).isBefore(LocalDateTime.now())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Interview time must be in the future.")
                .addPropertyNode("interviewTime")
                .addConstraintViolation();
        return false;
    }
}
