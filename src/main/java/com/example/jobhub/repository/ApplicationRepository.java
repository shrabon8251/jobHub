package com.example.jobhub.repository;

import com.example.jobhub.entity.Application;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    Optional<Application> findByJobIdAndJobSeekerId(Long jobId, Long jobSeekerId);

    @EntityGraph(attributePaths = {"job.recruiter.user", "job.category", "jobSeeker.user", "interview"})
    List<Application> findByJobSeekerUserIdOrderByAppliedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"job.recruiter.user", "job.category", "jobSeeker.user", "interview"})
    Optional<Application> findByIdAndJobSeekerUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"job.recruiter.user", "job.category", "jobSeeker.user", "interview"})
    List<Application> findByJobRecruiterUserIdOrderByAppliedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"job.recruiter.user", "job.category", "jobSeeker.user", "interview"})
    Optional<Application> findByIdAndJobRecruiterUserId(Long id, Long userId);
}
