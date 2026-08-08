package com.example.jobhub.repository;

import com.example.jobhub.entity.Experience;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    Optional<Experience> findByIdAndJobSeekerUserId(Long id, Long userId);

    boolean existsByJobSeekerIdAndCompanyAndPosition(
            Long jobSeekerId,
            String company,
            String position);
}
