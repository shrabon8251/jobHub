package com.example.jobhub.entity;

import com.example.jobhub.entity.embeddable.Address;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_seeker_profiles")
@Getter
@Setter
@NoArgsConstructor
public class JobSeekerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String fullName;

    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String phone;
    private String cvFilePath;
    private String resumeFilePath;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String whatsappNumber;

    @Embedded
    private Address address;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "jobSeeker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "job_seeker_skills",
            joinColumns = @JoinColumn(name = "job_seeker_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_job_seeker_skill", columnNames = {"job_seeker_id", "skill_id"}))
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "jobSeeker")
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "jobSeeker")
    private List<SavedJob> savedJobs = new ArrayList<>();
}
