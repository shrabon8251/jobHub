package com.example.jobhub.repository;

import com.example.jobhub.entity.JobSeekerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobSeekerProfileRepository extends JpaRepository<JobSeekerProfile, Long> {
    Optional<JobSeekerProfile> findByUserId(Long userId);
    Optional<JobSeekerProfile> findByProfilePicture(String profilePicture);
}
