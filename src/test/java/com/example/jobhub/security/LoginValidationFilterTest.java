package com.example.jobhub.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginValidationFilterTest {

    @Test
    void rejectsMalformedLoginBeforeAuthentication() throws Exception {
        LoginValidationFilter filter = new LoginValidationFilter(
                Validation.buildDefaultValidatorFactory().getValidator());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setParameter("email", "bad-email");
        request.setParameter("password", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("/login?validation", response.getRedirectedUrl());
        assertNotNull(request.getSession().getAttribute("loginValidationMessage"));
    }
}
