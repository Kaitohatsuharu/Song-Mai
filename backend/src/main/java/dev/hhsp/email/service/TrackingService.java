package dev.hhsp.email.service;

import dev.hhsp.email.entity.EmailEvent;
import dev.hhsp.email.entity.Message;
import dev.hhsp.email.repository.EmailEventRepository;
import dev.hhsp.email.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private static final Pattern HREF_PATTERN =
            Pattern.compile("href=\"(https?://[^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private final MessageRepository messageRepository;
    private final EmailEventRepository emailEventRepository;

    @Value("${app.tracking-base-url}")
    private String trackingBaseUrl;

    /**
     * Rewrites HTML body to inject open-tracking pixel and click-tracking redirects.
     */
    public String rewriteHtml(String html, String shortId) {
        // Rewrite links for click tracking
        StringBuffer sb = new StringBuffer();
        Matcher matcher = HREF_PATTERN.matcher(html);
        while (matcher.find()) {
            String originalUrl = matcher.group(1);
            String encodedUrl = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
            String trackingUrl = trackingBaseUrl + "/c/" + shortId + "?url=" + encodedUrl;
            matcher.appendReplacement(sb, "href=\"" + trackingUrl + "\"");
        }
        matcher.appendTail(sb);

        // Inject open-tracking pixel before </body>
        String openPixel = "<img src=\"" + trackingBaseUrl + "/o/" + shortId
                + "\" width=\"1\" height=\"1\" alt=\"\" style=\"display:none\"/>";
        String result = sb.toString();
        if (result.contains("</body>")) {
            result = result.replace("</body>", openPixel + "</body>");
        } else {
            result = result + openPixel;
        }
        return result;
    }

    @Transactional
    public void recordOpen(String shortId, String ip, String userAgent) {
        messageRepository.findByShortId(shortId).ifPresent(message -> {
            if (message.getOpenedAt() == null) {
                message.setOpenedAt(Instant.now());
                messageRepository.save(message);
            }
            recordEvent(message, "opened", Map.of("ip", ip, "user_agent", userAgent));
        });
    }

    @Transactional
    public Optional<String> recordClick(String shortId, String originalUrl, String ip) {
        Optional<Message> opt = messageRepository.findByShortId(shortId);
        opt.ifPresent(message -> {
            if (message.getClickedAt() == null) {
                message.setClickedAt(Instant.now());
                messageRepository.save(message);
            }
            recordEvent(message, "clicked",
                    Map.of("ip", ip, "url", originalUrl));
        });
        return opt.map(m -> originalUrl);
    }

    private void recordEvent(Message message, String eventType, Map<String, Object> metadata) {
        EmailEvent event = new EmailEvent();
        event.setMessage(message);
        event.setAccount(message.getAccount());
        event.setEventType(eventType);
        event.setMetadata(metadata);
        event.setOccurredAt(Instant.now());
        emailEventRepository.save(event);
    }
}
