package dev.hhsp.email.controller;

import dev.hhsp.email.dto.SendEmailRequest;
import dev.hhsp.email.dto.SendEmailResponse;
import dev.hhsp.email.entity.Account;
import dev.hhsp.email.entity.EmailEvent;
import dev.hhsp.email.entity.Message;
import dev.hhsp.email.repository.EmailEventRepository;
import dev.hhsp.email.repository.MessageRepository;
import dev.hhsp.email.service.EmailSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailSendService emailSendService;
    private final MessageRepository messageRepository;
    private final EmailEventRepository emailEventRepository;

    @PostMapping("/send")
    public ResponseEntity<SendEmailResponse> send(
            @AuthenticationPrincipal Account account,
            @Valid @RequestBody SendEmailRequest request) {
        SendEmailResponse response = emailSendService.send(account, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> getStatus(
            @AuthenticationPrincipal Account account,
            @PathVariable String messageId) {

        // Strip "msg_" prefix if present
        String rawId = messageId.startsWith("msg_") ? messageId.substring(4) : messageId;
        UUID id;
        try {
            id = UUID.fromString(rawId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return messageRepository.findById(id)
                .filter(m -> m.getAccount().getId().equals(account.getId()))
                .map(m -> {
                    List<EmailEvent> events = emailEventRepository.findByMessageIdOrderByOccurredAtAsc(m.getId());
                    Map<String, Object> body = Map.of(
                            "id", "msg_" + m.getId(),
                            "short_id", m.getShortId(),
                            "from", m.getFromAddress(),
                            "to", m.getToAddress(),
                            "subject", m.getSubject() != null ? m.getSubject() : "",
                            "status", m.getStatus(),
                            "created_at", m.getCreatedAt(),
                            "sent_at", m.getSentAt() != null ? m.getSentAt() : "",
                            "opened_at", m.getOpenedAt() != null ? m.getOpenedAt() : "",
                            "clicked_at", m.getClickedAt() != null ? m.getClickedAt() : "",
                            "events", events
                    );
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
