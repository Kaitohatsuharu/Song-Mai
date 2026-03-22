package dev.hhsp.email.controller;

import dev.hhsp.email.dto.ApiKeyCreateRequest;
import dev.hhsp.email.dto.ApiKeyCreateResponse;
import dev.hhsp.email.entity.Account;
import dev.hhsp.email.entity.ApiKey;
import dev.hhsp.email.entity.Message;
import dev.hhsp.email.entity.SendingDomain;
import dev.hhsp.email.repository.MessageRepository;
import dev.hhsp.email.repository.SendingDomainRepository;
import dev.hhsp.email.repository.WebhookRepository;
import dev.hhsp.email.service.ApiKeyService;
import dev.hhsp.email.service.QuotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final MessageRepository messageRepository;
    private final SendingDomainRepository sendingDomainRepository;
    private final WebhookRepository webhookRepository;
    private final ApiKeyService apiKeyService;
    private final QuotaService quotaService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> overview(@AuthenticationPrincipal Account account) {
        long totalMessages = messageRepository.countByAccountId(account.getId());
        long quotaUsed = quotaService.getEmailQuotaUsed(account.getId());

        return ResponseEntity.ok(Map.of(
                "plan", account.getPlan(),
                "quota_monthly", account.getQuotaMonthly(),
                "quota_used", quotaUsed,
                "total_messages", totalMessages
        ));
    }

    // --- Email Logs ---

    @GetMapping("/emails")
    public ResponseEntity<Page<Message>> listEmails(
            @AuthenticationPrincipal Account account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Message> messages = messageRepository.findByAccountId(
                account.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(messages);
    }

    // --- API Keys ---

    @GetMapping("/api-keys")
    public ResponseEntity<List<ApiKey>> listApiKeys(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(apiKeyService.listKeys(account.getId()));
    }

    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(
            @AuthenticationPrincipal Account account,
            @Valid @RequestBody ApiKeyCreateRequest request) {

        ApiKeyService.CreatedKey created = apiKeyService.createApiKey(account, request.getName());
        ApiKey key = created.apiKey();
        ApiKeyCreateResponse response = new ApiKeyCreateResponse(
                key.getId().toString(),
                key.getName(),
                created.rawKey(),
                key.getKeyPrefix(),
                key.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api-keys/{keyId}")
    public ResponseEntity<Void> deleteApiKey(
            @AuthenticationPrincipal Account account,
            @PathVariable UUID keyId) {
        apiKeyService.deleteKey(keyId, account.getId());
        return ResponseEntity.noContent().build();
    }

    // --- Sending Domains ---

    @GetMapping("/domains")
    public ResponseEntity<List<SendingDomain>> listDomains(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(sendingDomainRepository.findByAccountId(account.getId()));
    }

    @PostMapping("/domains")
    public ResponseEntity<SendingDomain> addDomain(
            @AuthenticationPrincipal Account account,
            @RequestBody Map<String, String> body) {

        String domain = body.get("domain");
        if (domain == null || domain.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (sendingDomainRepository.existsByAccountIdAndDomain(account.getId(), domain)) {
            return ResponseEntity.badRequest().build();
        }

        SendingDomain sd = new SendingDomain();
        sd.setAccount(account);
        sd.setDomain(domain.toLowerCase().trim());
        sendingDomainRepository.save(sd);
        return ResponseEntity.ok(sd);
    }

    // --- Webhooks ---

    @GetMapping("/webhooks")
    public ResponseEntity<Object> listWebhooks(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(webhookRepository.findByAccountId(account.getId()));
    }
}
