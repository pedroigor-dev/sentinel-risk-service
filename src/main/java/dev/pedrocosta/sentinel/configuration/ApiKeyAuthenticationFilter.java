package dev.pedrocosta.sentinel.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    static final String API_KEY_HEADER = "X-API-Key";

    private final byte[] expectedApiKey;

    public ApiKeyAuthenticationFilter(ApiKeyProperties properties) {
        this.expectedApiKey = properties.apiKey().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String suppliedApiKey = request.getHeader(API_KEY_HEADER);
        if (isValid(suppliedApiKey)) {
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            "api-client", null, List.of()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isValid(String suppliedApiKey) {
        if (suppliedApiKey == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expectedApiKey,
                suppliedApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
