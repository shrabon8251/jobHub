package com.example.jobhub.service;

import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.repository.JobSeekerProfileRepository;
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Coordinates media replacement/deletion while the storage service owns filesystem details. */
@Service
public class MediaService {

    private final MediaStorageService storage;
    private final JobSeekerProfileRepository jobSeekerProfiles;

    public MediaService(MediaStorageService storage, JobSeekerProfileRepository jobSeekerProfiles) {
        this.storage = storage;
        this.jobSeekerProfiles = jobSeekerProfiles;
    }

    public String replace(String previousFilename, MultipartFile file, String prefix) {
        String replacement = storage.store(file, prefix);
        if (previousFilename != null && !previousFilename.equals(replacement)) {
            storage.delete(previousFilename);
        }
        return replacement;
    }

    public void delete(String filename) {
        storage.delete(filename);
    }

    public Resource load(String filename) {
        return storage.load(filename);
    }

    public String contentType(String filename) {
        return storage.contentType(filename);
    }

    public boolean isPrivate(String filename) {
        return storage.isPrivate(filename);
    }

    public boolean canAccessPrivate(String filename, Authentication authentication) {
        if (!isAuthenticatedJobSeeker(authentication)) {
            return false;
        }
        return jobSeekerProfiles.findByProfilePicture(filename)
                .map(JobSeekerProfile::getUser)
                .filter(user -> user != null)
                .map(user -> Objects.equals(user.getEmail(), authentication.getName()))
                .orElse(false);
    }

    private boolean isAuthenticatedJobSeeker(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_JOB_SEEKER".equals(authority.getAuthority()));
    }
}
