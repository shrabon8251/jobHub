package com.example.jobhub.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ImageUploadValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageUpload {

    String message() default "Upload a JPG, PNG, WEBP, or GIF image no larger than 5 MB.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
