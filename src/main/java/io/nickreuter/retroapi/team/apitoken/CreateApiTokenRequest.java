package io.nickreuter.retroapi.team.apitoken;

import java.util.Set;

public record CreateApiTokenRequest(String name, Set<String> scopes) {}
