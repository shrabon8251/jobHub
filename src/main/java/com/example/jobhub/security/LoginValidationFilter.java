package com.example.jobhub.security;

import com.example.jobhub.dto.form.LoginForm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoginValidationFilter extends OncePerRequestFilter {

    private static final String ERROR_MESSAGE = "loginValidationMessage";
    private static final String ERROR_FIELD = "loginValidationField";

    private final Validator validator;

    public LoginValidationFilter(Validator validator) {
        this.validator = validator;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }
        return !"POST".equalsIgnoreCase(request.getMethod()) || !"/login".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        LoginForm form = new LoginForm();
        form.setEmail(request.getParameter("email"));
        form.setPassword(request.getParameter("password"));
        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);
        if (violations.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        ConstraintViolation<LoginForm> violation = violations.iterator().next();
        request.getSession().setAttribute(ERROR_MESSAGE, violation.getMessage());
        request.getSession().setAttribute(ERROR_FIELD, violation.getPropertyPath().toString());
        response.sendRedirect(request.getContextPath() + "/login?validation");
    }
}
