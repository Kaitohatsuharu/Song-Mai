package dev.hhsp.email.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "short_id", unique = true, nullable = false, length = 16)
    private String shortId;

    @Column(name = "message_id", length = 255)
    private String messageId;

    @Column(name = "from_address", nullable = false, length = 255)
    private String fromAddress;

    @Column(name = "to_address", nullable = false, length = 255)
    private String toAddress;

    @Column(length = 1000)
    private String subject;

    @Column(length = 50)
    private String status = "queued";

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "opened_at")
    private Instant openedAt;

    @Column(name = "clicked_at")
    private Instant clickedAt;

    @Column(name = "bounced_at")
    private Instant bouncedAt;

    @Column(name = "bounce_reason", columnDefinition = "TEXT")
    private String bounceReason;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
