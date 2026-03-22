package dev.hhsp.email.repository;

import dev.hhsp.email.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
    List<Webhook> findByAccountIdAndActiveTrue(UUID accountId);
    List<Webhook> findByAccountId(UUID accountId);
}
