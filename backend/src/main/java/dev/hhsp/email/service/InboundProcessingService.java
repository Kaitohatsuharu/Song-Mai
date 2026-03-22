package dev.hhsp.email.service;

import dev.hhsp.email.dto.InboundWebhookRequest;
import dev.hhsp.email.entity.Message;
import dev.hhsp.email.entity.Thread;
import dev.hhsp.email.repository.MessageRepository;
import dev.hhsp.email.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InboundProcessingService {

    private final MessageRepository messageRepository;
    private final ThreadRepository threadRepository;
    private final WebhookDeliveryService webhookDeliveryService;

    @Value("${app.nas-mount-path}")
    private String nasMountPath;

    @Transactional
    public void processInbound(InboundWebhookRequest request) {
        log.info("Processing inbound email from {} to {} (short_id={})",
                request.getFrom(), request.getTo(), request.getShortId());

        Optional<Message> originalMessage = Optional.empty();
        if (request.getShortId() != null) {
            originalMessage = messageRepository.findByShortId(request.getShortId());
        }

        Thread thread = new Thread();
        originalMessage.ifPresent(msg -> {
            thread.setAccount(msg.getAccount());
            thread.setOriginalMessage(msg);
        });

        thread.setFromAddress(request.getFrom());
        thread.setSubject(request.getSubject());
        thread.setTextBody(request.getText());
        thread.setHtmlBody(request.getHtml());
        thread.setRawHeaders(request.getHeaders());

        // Attachments would be saved to NAS and metadata stored here
        if (request.getAttachments() != null) {
            thread.setAttachments(request.getAttachments());
        }

        threadRepository.save(thread);

        // Deliver customer webhook notification
        if (thread.getAccount() != null) {
            webhookDeliveryService.deliver(
                    thread.getAccount().getId(),
                    "email.replied",
                    buildWebhookPayload(thread, request)
            );
        }
    }

    private java.util.Map<String, Object> buildWebhookPayload(Thread thread,
                                                               InboundWebhookRequest request) {
        return java.util.Map.of(
                "event", "email.replied",
                "thread_id", thread.getId().toString(),
                "from", request.getFrom(),
                "to", request.getTo(),
                "subject", request.getSubject() != null ? request.getSubject() : "",
                "received_at", thread.getReceivedAt().toString()
        );
    }
}
