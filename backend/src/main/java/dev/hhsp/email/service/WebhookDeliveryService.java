package dev.hhsp.email.service;

import dev.hhsp.email.entity.Webhook;
import dev.hhsp.email.repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final WebhookRepository webhookRepository;

    @Async
    public void deliver(UUID accountId, String eventType, Map<String, Object> payload) {
        List<Webhook> webhooks = webhookRepository.findByAccountIdAndActiveTrue(accountId);
        for (Webhook webhook : webhooks) {
            if (webhook.getEvents() != null && !webhook.getEvents().contains(eventType)) {
                continue;
            }
            try {
                deliverToEndpoint(webhook, eventType, payload);
            } catch (Exception e) {
                log.error("Failed to deliver webhook {} to {}: {}", eventType, webhook.getUrl(), e.getMessage());
            }
        }
    }

    private void deliverToEndpoint(Webhook webhook, String eventType,
                                   Map<String, Object> payload) throws Exception {
        String body = toJson(payload);
        String signature = signPayload(body, webhook.getSecret());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhook.getUrl()))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-HHSP-Event", eventType)
                .header("X-HHSP-Signature", "sha256=" + signature)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.warn("Webhook delivery to {} returned status {}", webhook.getUrl(), response.statusCode());
        } else {
            log.debug("Webhook {} delivered to {}", eventType, webhook.getUrl());
        }
    }

    private String signPayload(String payload, String secret) {
        if (secret == null || secret.isBlank()) return "";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Failed to sign webhook payload", e);
            return "";
        }
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        map.forEach((k, v) -> {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(k).append("\":\"").append(v).append("\"");
        });
        sb.append("}");
        return sb.toString();
    }
}
