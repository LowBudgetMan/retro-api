package io.nickreuter.retroapi.team.apitoken;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record ApiTokenView(
    UUID id, String name, String tokenPrefix, Set<String> scopes,
    Instant createdAt, Instant expiresAt, Instant lastUsedAt
) {
    public static ApiTokenView from(ApiTokenEntity e) {
        Set<String> scopes = Arrays.stream(e.getScopes().split(","))
            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        return new ApiTokenView(e.getId(), e.getName(), e.getTokenPrefix(), scopes,
            e.getCreatedAt(), e.getExpiresAt(), e.getLastUsedAt());
    }
}
