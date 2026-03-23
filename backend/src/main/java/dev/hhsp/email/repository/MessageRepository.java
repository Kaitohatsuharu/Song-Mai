package dev.hhsp.email.repository;

import dev.hhsp.email.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    Optional<Message> findByShortId(String shortId);
    Page<Message> findByAccountId(UUID accountId, Pageable pageable);
    long countByAccountId(UUID accountId);
}
