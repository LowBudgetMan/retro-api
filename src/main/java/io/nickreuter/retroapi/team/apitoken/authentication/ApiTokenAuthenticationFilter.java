package io.nickreuter.retroapi.team.apitoken.authentication;

import io.nickreuter.retroapi.team.apitoken.ApiTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiTokenService apiTokenService;

    public ApiTokenAuthenticationFilter(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var header = request.getHeader("Authorization");
            if (header != null && header.startsWith(BEARER_PREFIX)) {
                var token = header.substring(BEARER_PREFIX.length());
                if (token.startsWith(ApiTokenService.TOKEN_PREFIX)) {
                    apiTokenService.findByTokenString(token).ifPresent(entity -> {
                        Set<String> scopes = Arrays.stream(entity.getScopes().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toSet());
                        var auth = new ApiTokenAuthentication(entity.getId(), entity.getTeamId(), scopes);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        apiTokenService.touchLastUsed(entity.getId());
                    });
                }
            }
        }
        chain.doFilter(request, response);
    }
}
