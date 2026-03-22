package dev.hhsp.email.repository;

import dev.hhsp.email.entity.EmailEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailEventRepository extends JpaRepository<EmailEvent, UUID> {
    List<EmailEvent> findByMessageIdOrderByOccurredAtAsc(UUID messageId);
}
