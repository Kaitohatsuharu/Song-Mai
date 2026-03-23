package dev.hhsp.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiKeyCreateResponse {

    private String id;
    private String name;

    /** Full key — shown ONCE only at creation time */
    private String key;

    @JsonProperty("key_prefix")
    private String keyPrefix;

    @JsonProperty("created_at")
    private Instant createdAt;
}
