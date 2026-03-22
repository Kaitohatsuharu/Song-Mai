package dev.hhsp.email.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sending_domains")
@Data
@NoArgsConstructor
public class SendingDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 255)
    private String domain;

    @Column(name = "dkim_selector", length = 100)
    private String dkimSelector;

    @Column(name = "dkim_public", columnDefinition = "TEXT")
    private String dkimPublic;

    @Column(name = "spf_verified")
    private Boolean spfVerified = false;

    @Column(name = "dkim_verified")
    private Boolean dkimVerified = false;

    @Column(name = "dmarc_verified")
    private Boolean dmarcVerified = false;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
