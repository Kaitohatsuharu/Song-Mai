package dev.hhsp.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class SendEmailResponse {

    private String id;

    @JsonProperty("short_id")
    private String shortId;

    private String status;

    @JsonProperty("created_at")
    private Instant createdAt;
}
