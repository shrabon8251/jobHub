package com.example.jobhub.repository;

import com.example.jobhub.entity.SavedJob;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    @EntityGraph(attributePaths = {"job.recruiter", "job.category"})
    List<SavedJob> findByJobSeekerUserIdOrderBySavedAtDesc(Long userId);

    Optional<SavedJob> findByIdAndJobSeekerUserId(Long id, Long userId);
}
