package dev.hhsp.email.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "threads")
@Data
@NoArgsConstructor
public class Thread {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_msg")
    private Message originalMessage;

    @Column(name = "from_address", length = 255)
    private String fromAddress;

    @Column(length = 1000)
    private String subject;

    @Column(name = "text_body", columnDefinition = "TEXT")
    private String textBody;

    @Column(name = "html_body", columnDefinition = "TEXT")
    private String htmlBody;

    @Type(JsonBinaryType.class)
    @Column(name = "raw_headers", columnDefinition = "jsonb")
    private Map<String, Object> rawHeaders;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> attachments;

    @Column(name = "received_at")
    private Instant receivedAt = Instant.now();
}
