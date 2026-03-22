package dev.hhsp.email.controller;

import dev.hhsp.email.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Base64;

@RestController
@RequestMapping("/track")
@RequiredArgsConstructor
public class TrackingController {

    // 1x1 transparent GIF
    private static final byte[] TRANSPARENT_GIF = Base64.getDecoder().decode(
            "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

    private final TrackingService trackingService;

    @GetMapping("/o/{shortId}")
    public ResponseEntity<byte[]> trackOpen(
            @PathVariable String shortId,
            @RequestHeader(value = "X-Forwarded-For", defaultValue = "") String xff,
            @RequestHeader(value = "User-Agent", defaultValue = "") String userAgent) {

        String ip = xff.isBlank() ? "unknown" : xff.split(",")[0].trim();
        trackingService.recordOpen(shortId, ip, userAgent);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(TRANSPARENT_GIF);
    }

    @GetMapping("/c/{shortId}")
    public ResponseEntity<Void> trackClick(
            @PathVariable String shortId,
            @RequestParam(name = "url", required = false, defaultValue = "/") String url,
            @RequestHeader(value = "X-Forwarded-For", defaultValue = "") String xff) {

        String ip = xff.isBlank() ? "unknown" : xff.split(",")[0].trim();
        String destination = trackingService.recordClick(shortId, url, ip).orElse(url);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(destination))
                .build();
    }
}
