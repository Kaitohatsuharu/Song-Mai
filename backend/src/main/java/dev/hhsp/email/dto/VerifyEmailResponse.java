package dev.hhsp.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VerifyEmailResponse {

    private String email;
    private boolean valid;
    private boolean disposable;

    @JsonProperty("mx_found")
    private boolean mxFound;

    @JsonProperty("smtp_result")
    private String smtpResult;

    private int score;
}
