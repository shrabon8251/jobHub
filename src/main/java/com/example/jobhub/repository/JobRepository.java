package com.example.jobhub.repository;

import com.example.jobhub.entity.Job;
import com.example.jobhub.entity.enums.EmploymentType;
import com.example.jobhub.entity.enums.JobStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByTitleAndRecruiterId(String title, Long recruiterId);

    @EntityGraph(attributePaths = {"recruiter", "category"})
    @Query("""
            select j from Job j
            where j.status = :status and j.deadline >= :today
              and (:keyword is null or lower(j.title) like lower(concat('%', :keyword, '%'))
                   or lower(j.description) like lower(concat('%', :keyword, '%'))
                   or lower(j.recruiter.companyName) like lower(concat('%', :keyword, '%'))
                   or lower(j.category.name) like lower(concat('%', :keyword, '%')))
              and (:location is null or lower(j.location) like lower(concat('%', :location, '%')))
              and (:categoryId is null or j.category.id = :categoryId)
              and (:employmentType is null or j.employmentType = :employmentType)
              and (:salaryMin is null or j.salaryMax is null or j.salaryMax >= :salaryMin)
              and (:salaryMax is null or j.salaryMin is null or j.salaryMin <= :salaryMax)
            """)
    Page<Job> searchPublicJobs(@Param("status") JobStatus status, @Param("today") LocalDate today,
                               @Param("keyword") String keyword, @Param("location") String location,
                               @Param("categoryId") Long categoryId, @Param("employmentType") EmploymentType employmentType,
                               @Param("salaryMin") BigDecimal salaryMin, @Param("salaryMax") BigDecimal salaryMax,
                               Pageable pageable);

    @EntityGraph(attributePaths = {"recruiter", "category"})
    Optional<Job> findByIdAndStatusAndDeadlineGreaterThanEqual(Long id, JobStatus status, LocalDate today);

    @EntityGraph(attributePaths = {"recruiter", "category"})
    Optional<Job> findByIdAndRecruiterUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"recruiter", "category"})
    @Query("""
            select j from Job j
            where j.recruiter.user.id = :userId
            and (:keyword is null or lower(j.title) like lower(concat('%', :keyword, '%')))
            and (:status is null or j.status = :status)
            """)
    Page<Job> searchByRecruiterUserId(@Param("userId") Long userId, @Param("keyword") String keyword,
                                      @Param("status") JobStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"recruiter", "category"})
    @Query("""
            select j from Job j
            where (:query = '' or lower(j.title) like lower(concat('%', :query, '%'))
                   or lower(j.location) like lower(concat('%', :query, '%'))
                   or lower(j.recruiter.companyName) like lower(concat('%', :query, '%'))
                   or lower(j.category.name) like lower(concat('%', :query, '%')))
              and (:status is null or j.status = :status)
            """)
    Page<Job> searchAdminJobs(@Param("query") String query, @Param("status") JobStatus status,
                              Pageable pageable);

    long countByStatus(JobStatus status);
    boolean existsByCategoryId(Long categoryId);
}
