package dev.hhsp.email.controller;

import dev.hhsp.email.dto.InboundWebhookRequest;
import dev.hhsp.email.service.InboundProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/inbound")
@RequiredArgsConstructor
@Slf4j
public class InboundController {

    private final InboundProcessingService inboundProcessingService;

    @Value("${app.internal-secret}")
    private String internalSecret;

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveInbound(
            @RequestHeader("X-Internal-Secret") String secret,
            @RequestBody InboundWebhookRequest request) {

        if (!internalSecret.equals(secret)) {
            log.warn("Inbound webhook received with invalid secret");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        inboundProcessingService.processInbound(request);
        return ResponseEntity.ok().build();
    }
}
