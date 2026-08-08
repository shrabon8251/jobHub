package com.example.jobhub.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class HirvoAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public HirvoAuthenticationFailureHandler() {
        setDefaultFailureUrl("/login?error");
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String failureUrl = exception instanceof DisabledException
                ? "/login?disabled"
                : "/login?error";
        getRedirectStrategy().sendRedirect(request, response, failureUrl);
    }
}
