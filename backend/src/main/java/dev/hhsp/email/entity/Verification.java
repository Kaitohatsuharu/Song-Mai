package dev.hhsp.email.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verifications")
@Data
@NoArgsConstructor
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(nullable = false, length = 255)
    private String email;

    @Column
    private Boolean valid;

    @Column
    private Boolean disposable;

    @Column(name = "mx_found")
    private Boolean mxFound;

    @Column(name = "smtp_result", length = 50)
    private String smtpResult;

    @Column
    private Integer score;

    @Column(name = "cached_until")
    private Instant cachedUntil;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
