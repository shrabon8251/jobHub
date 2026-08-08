package com.example.jobhub.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import com.example.jobhub.exception.MediaValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaStorageService {

    private static final long DEFAULT_MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> IMAGE_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final String SAFE_PREFIX_REGEX = "[^a-zA-Z0-9-]";
    private static final String STORED_FILENAME_REGEX =
            "^[a-zA-Z0-9-]+-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                    + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|jpeg|png|webp|gif)$";

    private final Path root;
    private final long maxImageBytes;

    public MediaStorageService(
            @Value("${hirvo.media-dir:uploads}") String mediaDirectory,
            @Value("${hirvo.media.max-size:5242880}") long maxImageBytes) {
        this.root = Paths.get(mediaDirectory).toAbsolutePath().normalize();
        this.maxImageBytes = maxImageBytes > 0 ? maxImageBytes : DEFAULT_MAX_IMAGE_BYTES;
    }

    public String store(MultipartFile file, String prefix) {
        String extension = validateImage(file);
        String generatedName = createGeneratedName(extension, prefix);
        Path destination = resolveStoredFile(generatedName);
        try {
            Files.createDirectories(root);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }
            if (Files.size(destination) > maxImageBytes) {
                deleteQuietly(destination);
                throw new MediaValidationException("Images must be 5 MB or smaller.");
            }
            return generatedName;
        } catch (IOException exception) {
            deleteQuietly(destination);
            throw new MediaValidationException("The uploaded image could not be stored.");
        }
    }

    public Resource load(String filename) {
        if (!isSafeStoredFilename(filename)) {
            return null;
        }
        try {
            Path file = resolveStoredFile(filename);
            if (!Files.isRegularFile(file)) {
                return null;
            }
            Resource resource = new UrlResource(file.toUri());
            return resource.exists() && resource.isReadable() ? resource : null;
        } catch (IOException exception) {
            return null;
        }
    }

    public String contentType(String filename) {
        if (!isSafeStoredFilename(filename)) {
            return "application/octet-stream";
        }
        String extension = extensionOf(filename);
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    public void delete(String filename) {
        if (!isSafeStoredFilename(filename)) {
            return;
        }
        deleteQuietly(resolveStoredFile(filename));
    }

    public boolean isPrivate(String filename) {
        return filename != null && filename.startsWith("profile-");
    }

    private String validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new MediaValidationException("Choose an image to upload.");
        }
        if (file.getSize() > maxImageBytes) {
            throw new MediaValidationException("Images must be 5 MB or smaller.");
        }
        String contentType = normalizedContentType(file.getContentType());
        if (!IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new MediaValidationException("Upload a JPG, PNG, WEBP, or GIF image.");
        }
        String extension = extensionOfOriginalName(file.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension) || !extensionMatchesType(extension, contentType)) {
            throw new MediaValidationException("The file extension does not match its image type.");
        }
        ensureImageSignature(file, contentType);
        return extension;
    }

    private void ensureImageSignature(MultipartFile file, String contentType) {
        try {
            byte[] header = readHeader(file);
            if (!hasExpectedSignature(header, contentType)) {
                throw new MediaValidationException("The selected file is not a valid image.");
            }
        } catch (IOException exception) {
            throw new MediaValidationException("The selected image could not be read.");
        }
    }

    private byte[] readHeader(MultipartFile file) throws IOException {
        try (InputStream input = file.getInputStream()) {
            return input.readNBytes(12);
        }
    }

    private boolean hasExpectedSignature(byte[] header, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;
            case "image/png" -> startsWith(header, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
            case "image/gif" -> startsWith(header, new byte[]{'G', 'I', 'F', '8'});
            case "image/webp" -> startsWith(header, new byte[]{'R', 'I', 'F', 'F'})
                    && header.length >= 12
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            default -> false;
        };
    }

    private boolean startsWith(byte[] value, byte[] expected) {
        if (value.length < expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (value[index] != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private String createGeneratedName(String extension, String prefix) {
        String normalizedPrefix = prefix == null ? "image" : prefix.replaceAll(SAFE_PREFIX_REGEX, "");
        return normalizedPrefix + "-" + UUID.randomUUID() + "." + extension;
    }

    private Path resolveStoredFile(String filename) {
        Path file = root.resolve(filename).normalize();
        if (!file.startsWith(root)) {
            throw new IllegalArgumentException("The uploaded file could not be stored.");
        }
        return file;
    }

    private boolean isSafeStoredFilename(String filename) {
        return filename != null && !filename.isBlank()
                && filename.matches(STORED_FILENAME_REGEX);
    }

    private String normalizedContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).trim();
    }

    private String extensionOfOriginalName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()
                || originalFilename.contains("/") || originalFilename.contains("\\")
                || originalFilename.contains("..")) {
            throw new MediaValidationException("The uploaded filename is not valid.");
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot < 0 ? "" : originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String extensionOf(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private boolean extensionMatchesType(String extension, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "image/png" -> extension.equals("png");
            case "image/webp" -> extension.equals("webp");
            case "image/gif" -> extension.equals("gif");
            default -> false;
        };
    }

    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Missing or locked old media must not make a profile update fail.
        }
    }
}
