package com.example.jobhub.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.jobhub.dto.form.CategoryForm;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class MvcExceptionHandlerTest {

    private final MvcExceptionHandler handler = new MvcExceptionHandler();

    @Test
    void validationFailureBecomesFriendlyFlashMessage() {
        CategoryForm form = new CategoryForm();
        BindException binding = new BindException(form, "categoryForm");
        binding.rejectValue("name", "required", "Category name is required.");
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();

        String view = handler.validation(binding, flash);

        assertEquals("redirect:/", view);
        assertEquals("Category name is required.", flash.getFlashAttributes().get("error"));
    }

    @Test
    void ownershipAndMissingResourcesUseSafeErrorPages() {
        assertEquals("error/access-denied", handler.forbidden());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("error/not-found", handler.notFound(model));
        assertEquals("The requested resource is unavailable.", model.get("message"));
        assertTrue(!model.containsKey("stackTrace"));
    }
}
