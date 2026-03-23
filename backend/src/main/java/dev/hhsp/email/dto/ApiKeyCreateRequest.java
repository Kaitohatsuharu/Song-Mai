package dev.hhsp.email.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiKeyCreateRequest {

    @NotBlank
    private String name;
}
