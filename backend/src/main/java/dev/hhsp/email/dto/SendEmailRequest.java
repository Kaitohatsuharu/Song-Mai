package dev.hhsp.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SendEmailRequest {

    @NotBlank
    @Email
    private String from;

    @NotBlank
    @Email
    private String to;

    @NotBlank
    private String subject;

    private String html;
    private String text;
    private String replyTo;
    private List<String> tags;
    private Map<String, Object> metadata;
}
