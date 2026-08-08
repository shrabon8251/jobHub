package com.example.jobhub.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String JOB_SEEKER_DASHBOARD = "/seeker/dashboard";
    private static final String RECRUITER_DASHBOARD = "/recruiter/dashboard";
    private static final String ADMIN_DASHBOARD = "/admin/dashboard";

    private final RequestCache requestCache;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        var savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            redirectStrategy.sendRedirect(request, response, savedRequest.getRedirectUrl());
            return;
        }

        redirectStrategy.sendRedirect(request, response, dashboardFor(authentication));
    }

    private String dashboardFor(Authentication authentication) {
        boolean isJobSeeker = hasAuthority(authentication, "ROLE_JOB_SEEKER");
        if (isJobSeeker) {
            return JOB_SEEKER_DASHBOARD;
        }

        boolean isRecruiter = hasAuthority(authentication, "ROLE_RECRUITER");
        if (isRecruiter) {
            return RECRUITER_DASHBOARD;
        }

        return ADMIN_DASHBOARD;
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
