package dev.hhsp.email.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final StringRedisTemplate redis;

    @Value("${rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Bean
    public Filter rateLimitFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                    throws IOException, ServletException {

                HttpServletRequest httpReq = (HttpServletRequest) req;
                HttpServletResponse httpRes = (HttpServletResponse) res;

                String apiKey = httpReq.getHeader("X-API-Key");
                if (apiKey != null && !apiKey.isBlank()) {
                    String rateLimitKey = "ratelimit:" + apiKey + ":" + currentMinuteBucket();
                    Long count = redis.opsForValue().increment(rateLimitKey);
                    if (count == 1) {
                        redis.expire(rateLimitKey, Duration.ofMinutes(2));
                    }
                    if (count != null && count > requestsPerMinute) {
                        httpRes.setStatus(429);
                        httpRes.setContentType("application/json");
                        httpRes.getWriter().write("{\"error\":\"Rate limit exceeded\",\"limit\":" + requestsPerMinute + "}");
                        return;
                    }
                    httpRes.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                    httpRes.setHeader("X-RateLimit-Remaining",
                            String.valueOf(Math.max(0, requestsPerMinute - count)));
                }

                chain.doFilter(req, res);
            }

            private String currentMinuteBucket() {
                long epochMinute = System.currentTimeMillis() / 60_000L;
                return String.valueOf(epochMinute);
            }
        };
    }
}
