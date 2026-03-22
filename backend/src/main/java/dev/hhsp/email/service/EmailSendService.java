package dev.hhsp.email.service;

import dev.hhsp.email.dto.SendEmailRequest;
import dev.hhsp.email.dto.SendEmailResponse;
import dev.hhsp.email.entity.Account;
import dev.hhsp.email.entity.Message;
import dev.hhsp.email.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService {

    private static final String SEND_QUEUE_KEY = "queue:email:send";
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_ID_LENGTH = 12;

    private final MessageRepository messageRepository;
    private final QuotaService quotaService;
    private final TrackingService trackingService;
    private final StringRedisTemplate redis;

    @Value("${app.tracking-base-url}")
    private String trackingBaseUrl;

    @Transactional
    public SendEmailResponse send(Account account, SendEmailRequest request) {
        if (!quotaService.consumeEmailQuota(account.getId(), account.getQuotaMonthly())) {
            throw new QuotaExceededException("Monthly email quota exceeded");
        }

        String shortId = generateShortId();
        String replyTo = "reply-" + shortId + "@customer.hhsp.dev";

        // Rewrite HTML body to inject open/click tracking
        String processedHtml = request.getHtml() != null
                ? trackingService.rewriteHtml(request.getHtml(), shortId)
                : null;

        Message message = new Message();
        message.setAccount(account);
        message.setShortId(shortId);
        message.setFromAddress(request.getFrom());
        message.setToAddress(request.getTo());
        message.setSubject(request.getSubject());
        message.setStatus("queued");
        message.setCreatedAt(Instant.now());
        message = messageRepository.save(message);

        // Push to Redis queue for async delivery
        String jobPayload = buildJobPayload(message, request, processedHtml, replyTo);
        redis.opsForList().leftPush(SEND_QUEUE_KEY, jobPayload);

        log.info("Queued email message {} for account {}", message.getId(), account.getId());

        return new SendEmailResponse(
                "msg_" + message.getId(),
                message.getShortId(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }

    private String buildJobPayload(Message message, SendEmailRequest request,
                                   String processedHtml, String replyTo) {
        // Simple JSON payload — in production use Jackson ObjectMapper
        return String.format(
                "{\"message_id\":\"%s\",\"from\":\"%s\",\"to\":\"%s\"," +
                "\"subject\":\"%s\",\"reply_to\":\"%s\",\"has_html\":%b,\"has_text\":%b}",
                message.getId(),
                escape(request.getFrom()),
                escape(request.getTo()),
                escape(request.getSubject()),
                replyTo,
                processedHtml != null,
                request.getText() != null
        );
    }

    private String generateShortId() {
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(SHORT_ID_LENGTH);
        for (int i = 0; i < SHORT_ID_LENGTH; i++) {
            sb.append(ALPHABET.charAt(rng.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    public static class QuotaExceededException extends RuntimeException {
        public QuotaExceededException(String msg) { super(msg); }
    }
}
