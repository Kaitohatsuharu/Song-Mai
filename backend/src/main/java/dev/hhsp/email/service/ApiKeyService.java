package dev.hhsp.email.service;

import dev.hhsp.email.entity.Account;
import dev.hhsp.email.entity.ApiKey;
import dev.hhsp.email.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;

    public record CreatedKey(ApiKey apiKey, String rawKey) {}

    /**
     * Creates a new API key. Returns the entity plus the raw key (shown ONCE).
     */
    @Transactional
    public CreatedKey createApiKey(Account account, String name) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String rawKey = "sk_live_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String prefix = rawKey.substring(0, Math.min(12, rawKey.length()));

        ApiKey apiKey = new ApiKey();
        apiKey.setAccount(account);
        apiKey.setKeyHash(ENCODER.encode(rawKey));
        apiKey.setKeyPrefix(prefix);
        apiKey.setName(name);
        apiKeyRepository.save(apiKey);

        return new CreatedKey(apiKey, rawKey);
    }

    /**
     * Validates a raw API key. Returns the associated Account if valid.
     */
    public Optional<Account> validateApiKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) return Optional.empty();
        String prefix = rawKey.length() >= 12 ? rawKey.substring(0, 12) : rawKey;

        return apiKeyRepository.findAll().stream()
                .filter(k -> k.getKeyPrefix().equals(prefix) && ENCODER.matches(rawKey, k.getKeyHash()))
                .map(k -> {
                    k.setLastUsedAt(Instant.now());
                    apiKeyRepository.save(k);
                    return k.getAccount();
                })
                .findFirst();
    }

    public List<ApiKey> listKeys(UUID accountId) {
        return apiKeyRepository.findByAccountId(accountId);
    }

    @Transactional
    public void deleteKey(UUID keyId, UUID accountId) {
        apiKeyRepository.findById(keyId).ifPresent(key -> {
            if (key.getAccount().getId().equals(accountId)) {
                apiKeyRepository.delete(key);
            }
        });
    }
}
