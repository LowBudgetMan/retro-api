package io.nickreuter.retroapi.team.apitoken;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CreateApiTokenResponse(
    UUID id, String name, Set<String> scopes, Instant expiresAt,
    String tokenPrefix, String token
) {}
