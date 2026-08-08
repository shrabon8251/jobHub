package com.example.jobhub.repository;

import com.example.jobhub.entity.Education;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
    Optional<Education> findByIdAndJobSeekerUserId(Long id, Long userId);

    boolean existsByJobSeekerIdAndDegreeAndInstitution(
            Long jobSeekerId,
            String degree,
            String institution);
}
