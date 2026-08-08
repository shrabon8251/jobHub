package com.example.jobhub.repository;

import com.example.jobhub.entity.Interview;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    @EntityGraph(attributePaths = {
            "application.job.recruiter.user",
            "application.job.category",
            "application.jobSeeker.user"})
    Optional<Interview> findByIdAndApplicationJobSeekerUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {
            "application.job.recruiter.user",
            "application.job.category",
            "application.jobSeeker.user"})
    Optional<Interview> findByIdAndApplicationJobRecruiterUserId(Long id, Long userId);

    Optional<Interview> findByApplicationId(Long applicationId);
}
