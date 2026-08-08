package com.example.jobhub.service;

import com.example.jobhub.dto.form.RegistrationForm;
import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.entity.RecruiterProfile;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.exception.DuplicateEmailException;
import com.example.jobhub.repository.JobSeekerProfileRepository;
import com.example.jobhub.repository.RecruiterProfileRepository;
import com.example.jobhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository users;
    private final JobSeekerProfileRepository seekers;
    private final RecruiterProfileRepository recruiters;
    private final PasswordEncoder encoder;

    @Transactional
    public void register(RegistrationForm form) {
        validateRole(form.getRole());
        String email = normalizeEmail(form.getEmail());
        ensureEmailIsAvailable(email);
        User user = createUser(form, email);
        createProfile(form, user);
    }

    private void validateRole(Role role) {
        if (role != Role.JOB_SEEKER && role != Role.RECRUITER) {
            throw new IllegalArgumentException("Only job seeker and recruiter registration is available.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void ensureEmailIsAvailable(String email) {
        if (users.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
    }

    private User createUser(RegistrationForm form, String email) {
        User user = new User();
        user.setName(form.getName().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(form.getPassword()));
        user.setRole(form.getRole());
        return users.save(user);
    }

    private void createProfile(RegistrationForm form, User user) {
        if (user.getRole() == Role.JOB_SEEKER) {
            createJobSeekerProfile(form, user);
            return;
        }
        createRecruiterProfile(form, user);
    }

    private void createJobSeekerProfile(RegistrationForm form, User user) {
        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUser(user);
        profile.setFullName(form.getProfileName().trim());
        seekers.save(profile);
    }

    private void createRecruiterProfile(RegistrationForm form, User user) {
        RecruiterProfile profile = new RecruiterProfile();
        profile.setUser(user);
        profile.setCompanyName(form.getProfileName().trim());
        recruiters.save(profile);
    }
}
