package com.example.jobhub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public class ImageUploadValidator implements ConstraintValidator<ValidImageUpload, MultipartFile> {

    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final Map<String, Set<String>> EXTENSIONS_BY_TYPE = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp"),
            "image/gif", Set.of("gif"));

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }
        String contentType = normalize(file.getContentType());
        String extension = extension(file.getOriginalFilename());
        return file.getSize() <= MAX_BYTES
                && EXTENSIONS_BY_TYPE.containsKey(contentType)
                && EXTENSIONS_BY_TYPE.get(contentType).contains(extension);
    }

    private String normalize(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).trim();
    }

    private String extension(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("/") || filename.contains("\\")) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
