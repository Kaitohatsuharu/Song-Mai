package dev.hhsp.email.service;

import dev.hhsp.email.dto.auth.AuthResponse;
import dev.hhsp.email.dto.auth.LoginRequest;
import dev.hhsp.email.dto.auth.RegisterRequest;
import dev.hhsp.email.entity.Account;
import dev.hhsp.email.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest req) {
        if (accountRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        Account account = new Account();
        account.setEmail(req.getEmail());
        account.setName(req.getName());
        account.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        accountRepository.save(account);
    }

    public AuthResponse login(LoginRequest req) {
        Account account = accountRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (account.getPasswordHash() == null ||
                !passwordEncoder.matches(req.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(account.getId(), account.getEmail());
        return new AuthResponse(
                token,
                new AuthResponse.UserInfo(
                        account.getId().toString(),
                        account.getEmail(),
                        account.getName()));
    }
}
