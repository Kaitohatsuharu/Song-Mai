package dev.hhsp.email.service;

import dev.hhsp.email.entity.Account;
import dev.hhsp.email.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private static final String QUOTA_KEY_PREFIX = "quota:";

    private final AccountRepository accountRepository;
    private final StringRedisTemplate redis;

    /**
     * Atomically decrements quota in Redis.
     * Returns true if quota is available and was consumed, false if quota exhausted.
     */
    public boolean consumeEmailQuota(UUID accountId, int quotaMonthly) {
        String key = QUOTA_KEY_PREFIX + accountId;
        Long used = redis.opsForValue().increment(key);
        if (used == null || used > quotaMonthly) {
            // Rollback the increment
            redis.opsForValue().decrement(key);
            return false;
        }
        return true;
    }

    public boolean consumeVerificationQuota(UUID accountId) {
        // Verifications use a separate counter; reuse email quota approach
        String key = "verify_quota:" + accountId;
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        int verifyQuota = switch (account.getPlan()) {
            case "growth" -> 10_000;
            case "pro" -> Integer.MAX_VALUE;
            default -> 1_000; // starter
        };
        Long used = redis.opsForValue().increment(key);
        if (used == null || used > verifyQuota) {
            redis.opsForValue().decrement(key);
            return false;
        }
        return true;
    }

    public long getEmailQuotaUsed(UUID accountId) {
        String val = redis.opsForValue().get(QUOTA_KEY_PREFIX + accountId);
        return val == null ? 0L : Long.parseLong(val);
    }
}
