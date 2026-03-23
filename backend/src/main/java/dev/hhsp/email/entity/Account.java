package dev.hhsp.email.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(length = 50)
    private String plan = "starter";

    @Column(name = "quota_monthly")
    private Integer quotaMonthly = 10000;

    @Column(name = "quota_used")
    private Integer quotaUsed = 0;

    @Column(name = "quota_reset")
    private Instant quotaReset;

    @Column(name = "stripe_id", length = 255)
    private String stripeId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
