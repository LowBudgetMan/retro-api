package io.nickreuter.retroapi.team.apitoken;

import java.util.Set;
import java.util.UUID;

public record CreateApiTokenResponse(
    UUID id, String name, Set<String> scopes, String tokenPrefix, String token
) {}
