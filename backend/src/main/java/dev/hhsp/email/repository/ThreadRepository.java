package dev.hhsp.email.repository;

import dev.hhsp.email.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, UUID> {
    List<Thread> findByOriginalMessageId(UUID messageId);
    List<Thread> findByAccountIdOrderByReceivedAtDesc(UUID accountId);
}
