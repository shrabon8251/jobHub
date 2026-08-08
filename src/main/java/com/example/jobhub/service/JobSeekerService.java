package com.example.jobhub.service;

import com.example.jobhub.dto.form.EducationForm;
import com.example.jobhub.dto.form.ExperienceForm;
import com.example.jobhub.dto.form.ProfileForm;
import com.example.jobhub.dto.form.SkillForm;
import com.example.jobhub.entity.Education;
import com.example.jobhub.entity.Experience;
import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.entity.Skill;
import com.example.jobhub.entity.User;
import com.example.jobhub.entity.embeddable.Address;
import com.example.jobhub.entity.enums.Role;
import com.example.jobhub.exception.ForbiddenOwnershipException;
import com.example.jobhub.exception.ResourceNotFoundException;
import com.example.jobhub.repository.EducationRepository;
import com.example.jobhub.repository.ExperienceRepository;
import com.example.jobhub.repository.JobSeekerProfileRepository;
import com.example.jobhub.repository.SkillRepository;
import java.time.Year;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobSeekerService {

    private static final int MAX_ALLOWED_START_YEAR_OFFSET = 1;

    private final CurrentUserService current;
    private final JobSeekerProfileRepository profiles;
    private final EducationRepository educations;
    private final ExperienceRepository experiences;
    private final SkillRepository skills;
    private final MediaService media;

    private User requireJobSeeker() {
        User user = current.requireCurrentUser();
        if (user.getRole() != Role.JOB_SEEKER) {
            throw new ForbiddenOwnershipException();
        }
        return user;
    }

    @Transactional(readOnly = true)
    public JobSeekerProfile profile() {
        User user = requireJobSeeker();
        JobSeekerProfile profile = profiles.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile was not found."));
        initializeProfileCollections(profile);
        return profile;
    }

    @Transactional
    public void updateProfile(ProfileForm form) {
        JobSeekerProfile profile = profile();
        applyProfileFields(profile, form);
        applyAddress(profile, form);
        storeProfileImage(profile, form);
    }

    @Transactional
    public void deleteProfilePhoto() {
        JobSeekerProfile profile = profile();
        media.delete(profile.getProfilePicture());
        profile.setProfilePicture(null);
    }

    @Transactional
    public Education addEducation(EducationForm form) {
        Education education = new Education();
        education.setJobSeeker(profile());
        applyEducationFields(education, form);
        return educations.save(education);
    }

    @Transactional
    public void updateEducation(Long id, EducationForm form) {
        applyEducationFields(education(id), form);
    }

    @Transactional
    public void deleteEducation(Long id) {
        educations.delete(education(id));
    }

    public Education education(Long id) {
        return educations.findByIdAndJobSeekerUserId(id, requireJobSeeker().getId())
                .orElseThrow(ForbiddenOwnershipException::new);
    }

    @Transactional
    public Experience addExperience(ExperienceForm form) {
        Experience experience = new Experience();
        experience.setJobSeeker(profile());
        applyExperienceFields(experience, form);
        return experiences.save(experience);
    }

    @Transactional
    public void updateExperience(Long id, ExperienceForm form) {
        applyExperienceFields(experience(id), form);
    }

    @Transactional
    public void deleteExperience(Long id) {
        experiences.delete(experience(id));
    }

    public Experience experience(Long id) {
        return experiences.findByIdAndJobSeekerUserId(id, requireJobSeeker().getId())
                .orElseThrow(ForbiddenOwnershipException::new);
    }

    @Transactional
    public void addSkill(SkillForm form) {
        JobSeekerProfile profile = profile();
        String skillName = normalizeSkillName(form.getName());
        Skill skill = findOrCreateSkill(skillName);
        ensureSkillIsNotAlreadyAttached(profile, skill);
        profile.getSkills().add(skill);
    }

    @Transactional
    public void removeSkill(Long id) {
        JobSeekerProfile profile = profile();
        Skill skill = profile.getSkills().stream()
                .filter(existing -> existing.getId().equals(id))
                .findFirst()
                .orElseThrow(ForbiddenOwnershipException::new);
        profile.getSkills().remove(skill);
    }

    private void initializeProfileCollections(JobSeekerProfile profile) {
        profile.getSkills().size();
        profile.getEducations().size();
        profile.getExperiences().size();
    }

    private void applyProfileFields(JobSeekerProfile profile, ProfileForm form) {
        profile.setFullName(form.getFullName().trim());
        profile.setPhone(trim(form.getPhone()));
        profile.setBio(trim(form.getBio()));
        profile.setLinkedinUrl(trim(form.getLinkedinUrl()));
        profile.setGithubUrl(trim(form.getGithubUrl()));
        profile.setPortfolioUrl(trim(form.getPortfolioUrl()));
        profile.setWhatsappNumber(trim(form.getWhatsappNumber()));
    }

    private void applyAddress(JobSeekerProfile profile, ProfileForm form) {
        Address address = profile.getAddress() == null ? new Address() : profile.getAddress();
        address.setStreet(trim(form.getStreet()));
        address.setCity(trim(form.getCity()));
        address.setCountry(trim(form.getCountry()));
        address.setPostalCode(trim(form.getPostalCode()));
        profile.setAddress(address);
    }

    private void storeProfileImage(JobSeekerProfile profile, ProfileForm form) {
        if (form.getProfileImage() != null && !form.getProfileImage().isEmpty()) {
            profile.setProfilePicture(media.replace(
                    profile.getProfilePicture(), form.getProfileImage(), "profile"));
        }
    }

    private void applyEducationFields(Education education, EducationForm form) {
        validateEducationDates(form);
        education.setDegree(form.getDegree().trim());
        education.setInstitution(form.getInstitution().trim());
        education.setFieldOfStudy(trim(form.getFieldOfStudy()));
        education.setStartYear(form.getStartYear());
        education.setEndYear(form.getEndYear());
    }

    private void validateEducationDates(EducationForm form) {
        if (form.getEndYear() != null && form.getEndYear() < form.getStartYear()) {
            throw new IllegalArgumentException("End year must not be before start year.");
        }
        if (form.getStartYear() > Year.now().getValue() + MAX_ALLOWED_START_YEAR_OFFSET) {
            throw new IllegalArgumentException("Start year is not valid.");
        }
    }

    private void applyExperienceFields(Experience experience, ExperienceForm form) {
        if (form.getEndDate() != null && form.getEndDate().isBefore(form.getStartDate())) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }
        experience.setCompany(form.getCompany().trim());
        experience.setPosition(form.getPosition().trim());
        experience.setDescription(trim(form.getDescription()));
        experience.setStartDate(form.getStartDate());
        experience.setEndDate(form.getEndDate());
    }

    private String normalizeSkillName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private Skill findOrCreateSkill(String name) {
        return skills.findByName(name).orElseGet(() -> {
            Skill skill = new Skill();
            skill.setName(name);
            return skills.save(skill);
        });
    }

    private void ensureSkillIsNotAlreadyAttached(JobSeekerProfile profile, Skill skill) {
        if (profile.getSkills().contains(skill)) {
            throw new IllegalArgumentException("This skill is already on your profile.");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
