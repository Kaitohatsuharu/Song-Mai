package dev.hhsp.email.repository;

import dev.hhsp.email.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    Optional<Verification> findTopByEmailAndCachedUntilAfterOrderByCreatedAtDesc(
            String email, Instant now);
}
