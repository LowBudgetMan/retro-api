package io.nickreuter.retroapi.share.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.UUID;

public class ShareTokenAuthentication extends AbstractAuthenticationToken {
    private final String token;
    private final UUID retroId;
    private final String name;

    public ShareTokenAuthentication(String token, UUID retroId, String name) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
        this.retroId = retroId;
        this.name = name;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    public UUID getRetroId() {
        return retroId;
    }
}
