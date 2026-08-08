package com.example.jobhub.controller;

import com.example.jobhub.service.MediaService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import java.time.Duration;

@RestController
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/media/{filename:.+}")
    public ResponseEntity<Resource> image(@PathVariable String filename, Authentication authentication) {
        if (mediaService.isPrivate(filename) && !mediaService.canAccessPrivate(filename, authentication)) {
            return ResponseEntity.status(403).build();
        }
        Resource resource = mediaService.load(filename);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType type;
        try {
            type = MediaType.parseMediaType(mediaService.contentType(filename));
        } catch (IllegalArgumentException ex) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        CacheControl cacheControl = mediaService.isPrivate(filename)
                ? CacheControl.noCache().cachePrivate()
                : CacheControl.maxAge(Duration.ofHours(12)).cachePublic();
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(type)
                .body(resource);
    }

}
