package io.nickreuter.retroapi.team.apitoken.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ApiTokenAuthentication extends AbstractAuthenticationToken {
    private final UUID tokenId;
    private final UUID teamId;
    private final Set<String> scopes;

    public ApiTokenAuthentication(UUID tokenId, UUID teamId, Set<String> scopes) {
        super(scopes.stream().map(s -> new SimpleGrantedAuthority("SCOPE_" + s)).collect(Collectors.toSet()));
        this.tokenId = tokenId;
        this.teamId = teamId;
        this.scopes = scopes;
        setAuthenticated(true);
    }

    public UUID getTokenId() { return tokenId; }
    public UUID getTeamId() { return teamId; }
    public Set<String> getScopes() { return scopes; }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return getName(); }

    @Override
    public String getName() { return "api-token:" + tokenId; }
}
