package com.example.jobhub.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.jobhub.exception.MediaValidationException;
import com.example.jobhub.controller.MediaController;
import com.example.jobhub.entity.JobSeekerProfile;
import com.example.jobhub.entity.User;
import com.example.jobhub.repository.JobSeekerProfileRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.TestingAuthenticationToken;

class MediaStorageServiceTest {

    private static final byte[] ONE_BY_ONE_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    @TempDir
    Path mediaDirectory;

    @Test
    void storesUuidNamedImageAndServesIt() throws Exception {
        MediaStorageService storage = storage();

        String filename = storage.store(image("avatar.png", "image/png", ONE_BY_ONE_PNG), "profile");

        assertTrue(filename.matches("profile-[0-9a-f-]{36}\\.png"));
        assertTrue(Files.isRegularFile(mediaDirectory.resolve(filename)));
        assertTrue(storage.load(filename).exists());
        assertTrue(storage.isPrivate(filename));
    }

    @Test
    void replacementDeletesPreviousFileAndDeleteIsIdempotent() throws Exception {
        MediaStorageService storage = storage();
        MediaService media = new MediaService(storage, mock(JobSeekerProfileRepository.class));
        String previous = media.replace(null, image("old.png", "image/png", ONE_BY_ONE_PNG), "job");

        String replacement = media.replace(previous, image("new.png", "image/png", ONE_BY_ONE_PNG), "job");

        assertNotEquals(previous, replacement);
        assertFalse(Files.exists(mediaDirectory.resolve(previous)));
        assertTrue(Files.exists(mediaDirectory.resolve(replacement)));
        media.delete(replacement);
        media.delete(replacement);
        assertNull(storage.load(replacement));
    }

    @Test
    void rejectsInvalidMimeExtensionSignatureAndOversizedFiles() {
        MediaStorageService storage = storage();

        assertThrows(MediaValidationException.class,
                () -> storage.store(image("script.png", "text/plain", "not an image".getBytes()), "profile"));
        assertThrows(MediaValidationException.class,
                () -> storage.store(image("wrong.jpg", "image/png", ONE_BY_ONE_PNG), "profile"));
        assertThrows(MediaValidationException.class,
                () -> storage.store(image("fake.png", "image/png", "not an image".getBytes()), "profile"));

        MediaStorageService sizeLimitedStorage = new MediaStorageService(mediaDirectory.toString(), 10);
        assertThrows(MediaValidationException.class,
                () -> sizeLimitedStorage.store(image("large.png", "image/png", ONE_BY_ONE_PNG), "profile"));
    }

    @Test
    void safelyHandlesTraversalAndMissingFiles() {
        MediaStorageService storage = storage();

        assertNull(storage.load("../outside.png"));
        assertNull(storage.load("missing-00000000-0000-0000-0000-000000000000.png"));
        storage.delete("../../outside.png");
    }

    @Test
    void protectsPrivateMediaWhileKeepingBrandingPublic() {
        MediaStorageService storage = storage();
        JobSeekerProfileRepository profiles = mock(JobSeekerProfileRepository.class);
        MediaService media = new MediaService(storage, profiles);
        MediaController controller = new MediaController(media);
        String profile = media.replace(null, image("profile.png", "image/png", ONE_BY_ONE_PNG), "profile");
        String logo = media.replace(null, image("logo.png", "image/png", ONE_BY_ONE_PNG), "company-logo");
        User owner = new User();
        owner.setEmail("owner@example.com");
        JobSeekerProfile ownerProfile = new JobSeekerProfile();
        ownerProfile.setUser(owner);
        ownerProfile.setProfilePicture(profile);
        when(profiles.findByProfilePicture(profile)).thenReturn(Optional.of(ownerProfile));
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        ResponseEntity<Resource> privateResponse = controller.image(profile, anonymous);
        ResponseEntity<Resource> publicResponse = controller.image(logo, anonymous);

        assertEquals(403, privateResponse.getStatusCode().value());
        assertEquals(200, publicResponse.getStatusCode().value());
        assertTrue(publicResponse.getHeaders().getFirst("Cache-Control").contains("public"));
        assertEquals(403, controller.image(profile,
                new TestingAuthenticationToken("other@example.com", "password", "ROLE_JOB_SEEKER"))
                .getStatusCode().value());
        assertTrue(controller.image(profile,
                new TestingAuthenticationToken("owner@example.com", "password", "ROLE_JOB_SEEKER"))
                .getHeaders().getFirst("Cache-Control").contains("private"));
    }

    private MediaStorageService storage() {
        return new MediaStorageService(mediaDirectory.toString(), 5 * 1024 * 1024);
    }

    private MockMultipartFile image(String name, String contentType, byte[] content) {
        return new MockMultipartFile("image", name, contentType, content);
    }
}
