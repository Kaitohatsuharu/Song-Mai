package dev.hhsp.email.controller;

import dev.hhsp.email.dto.VerifyEmailRequest;
import dev.hhsp.email.dto.VerifyEmailResponse;
import dev.hhsp.email.entity.Account;
import dev.hhsp.email.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/verify")
@RequiredArgsConstructor
public class VerifyController {

    private final EmailVerificationService verificationService;

    @PostMapping
    public ResponseEntity<VerifyEmailResponse> verify(
            @AuthenticationPrincipal Account account,
            @Valid @RequestBody VerifyEmailRequest request) {
        VerifyEmailResponse response = verificationService.verify(account, request.getEmail());
        return ResponseEntity.ok(response);
    }
}
