package dev.hhsp.email.service;

import dev.hhsp.email.dto.VerifyEmailResponse;
import dev.hhsp.email.entity.Account;
import dev.hhsp.email.entity.Verification;
import dev.hhsp.email.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    private static final int CACHE_HOURS = 24;
    private static final int SMTP_TIMEOUT_MS = 10_000;

    // Basic disposable domain list — extend via database or external list
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "guerrillamail.com", "tempmail.com",
            "throwaway.email", "yopmail.com", "sharklasers.com",
            "guerrillamailblock.com", "grr.la", "guerrillamail.info",
            "spam4.me", "trashmail.com", "dispostable.com"
    );

    private final VerificationRepository verificationRepository;
    private final QuotaService quotaService;

    @Transactional
    public VerifyEmailResponse verify(Account account, String email) {
        // Check cache first
        Optional<Verification> cached = verificationRepository
                .findTopByEmailAndCachedUntilAfterOrderByCreatedAtDesc(email, Instant.now());
        if (cached.isPresent()) {
            return toResponse(cached.get());
        }

        // Consume quota
        if (!quotaService.consumeVerificationQuota(account.getId())) {
            throw new QuotaExceededException("Verification quota exceeded");
        }

        Verification result = doVerify(email);
        result.setAccount(account);
        result.setCachedUntil(Instant.now().plusSeconds(CACHE_HOURS * 3600L));
        verificationRepository.save(result);

        return toResponse(result);
    }

    private Verification doVerify(String email) {
        Verification v = new Verification();
        v.setEmail(email);

        // Step 1: Syntax
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            v.setValid(false);
            v.setScore(0);
            v.setSmtpResult("undeliverable");
            return v;
        }

        String domain = email.substring(email.indexOf('@') + 1);

        // Step 2: MX Lookup
        List<String> mxRecords = lookupMX(domain);
        v.setMxFound(!mxRecords.isEmpty());
        if (mxRecords.isEmpty()) {
            v.setSmtpResult("undeliverable");
            v.setScore(10);
            v.setValid(false);
            return v;
        }

        // Step 3: Disposable check
        v.setDisposable(DISPOSABLE_DOMAINS.contains(domain.toLowerCase()));

        // Step 4: SMTP Handshake (no actual send)
        String smtpResult = smtpHandshakeCheck(mxRecords.get(0), email);
        v.setSmtpResult(smtpResult);

        // Step 5: Calculate score
        int score = calculateScore(v.getMxFound(), v.getDisposable(), smtpResult);
        v.setScore(score);
        v.setValid(score >= 60);

        return v;
    }

    private List<String> lookupMX(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            InitialDirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes("dns:/" + domain, new String[]{"MX"});
            javax.naming.directory.Attribute mxAttr = attrs.get("MX");
            if (mxAttr == null) return List.of();

            List<String[]> records = new ArrayList<>();
            for (int i = 0; i < mxAttr.size(); i++) {
                String record = mxAttr.get(i).toString();
                String[] parts = record.split("\\s+");
                if (parts.length >= 2) {
                    records.add(parts);
                }
            }
            // Sort by priority
            records.sort(Comparator.comparingInt(a -> Integer.parseInt(a[0])));
            return records.stream().map(a -> a[1]).toList();
        } catch (Exception e) {
            log.debug("MX lookup failed for domain {}: {}", domain, e.getMessage());
            return List.of();
        }
    }

    private String smtpHandshakeCheck(String mxHost, String email) {
        // Remove trailing dot from MX hostname if present
        String host = mxHost.endsWith(".") ? mxHost.substring(0, mxHost.length() - 1) : mxHost;
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, 25), SMTP_TIMEOUT_MS);
            socket.setSoTimeout(SMTP_TIMEOUT_MS);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            readMultiline(in);                           // 220 greeting
            out.println("EHLO verify.hhsp.dev");
            readMultiline(in);                           // 250 capabilities
            out.println("MAIL FROM:<verify@hhsp.dev>");
            readLine(in);                                // 250 ok
            out.println("RCPT TO:<" + email + ">");
            String rcptResponse = readLine(in);
            out.println("QUIT");

            if (rcptResponse.startsWith("250")) return "deliverable";
            if (rcptResponse.startsWith("550") || rcptResponse.startsWith("551")
                    || rcptResponse.startsWith("553")) return "undeliverable";
            return "risky";
        } catch (Exception e) {
            log.debug("SMTP handshake failed for {}: {}", mxHost, e.getMessage());
            return "unknown";
        }
    }

    private String readLine(BufferedReader in) throws Exception {
        String line = in.readLine();
        return line != null ? line : "";
    }

    private void readMultiline(BufferedReader in) throws Exception {
        String line;
        do {
            line = in.readLine();
        } while (line != null && line.length() >= 4 && line.charAt(3) == '-');
    }

    private int calculateScore(boolean mxFound, boolean disposable, String smtpResult) {
        int score = 0;
        if (mxFound) score += 30;
        if (!disposable) score += 20;
        score += switch (smtpResult) {
            case "deliverable" -> 50;
            case "risky" -> 20;
            case "unknown" -> 10;
            default -> 0;
        };
        return Math.min(score, 100);
    }

    private VerifyEmailResponse toResponse(Verification v) {
        VerifyEmailResponse r = new VerifyEmailResponse();
        r.setEmail(v.getEmail());
        r.setValid(Boolean.TRUE.equals(v.getValid()));
        r.setDisposable(Boolean.TRUE.equals(v.getDisposable()));
        r.setMxFound(Boolean.TRUE.equals(v.getMxFound()));
        r.setSmtpResult(v.getSmtpResult());
        r.setScore(v.getScore() != null ? v.getScore() : 0);
        return r;
    }

    public static class QuotaExceededException extends RuntimeException {
        public QuotaExceededException(String msg) { super(msg); }
    }
}
