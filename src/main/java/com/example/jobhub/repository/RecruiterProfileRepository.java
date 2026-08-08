package com.example.jobhub.repository;

import com.example.jobhub.entity.RecruiterProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruiterProfileRepository extends JpaRepository<RecruiterProfile, Long> {
    Optional<RecruiterProfile> findByUserId(Long userId);
}
