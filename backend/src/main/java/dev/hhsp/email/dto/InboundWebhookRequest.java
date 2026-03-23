package dev.hhsp.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InboundWebhookRequest {

    private String to;
    private String from;

    @JsonProperty("short_id")
    private String shortId;

    private String subject;
    private String text;
    private String html;
    private Map<String, Object> headers;
    private List<Map<String, Object>> attachments;
}
