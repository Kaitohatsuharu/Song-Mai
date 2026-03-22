package dev.hhsp.email.repository;

import dev.hhsp.email.entity.SendingDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SendingDomainRepository extends JpaRepository<SendingDomain, UUID> {
    List<SendingDomain> findByAccountId(UUID accountId);
    Optional<SendingDomain> findByAccountIdAndDomain(UUID accountId, String domain);
    boolean existsByAccountIdAndDomain(UUID accountId, String domain);
}
