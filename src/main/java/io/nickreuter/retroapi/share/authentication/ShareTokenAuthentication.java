package io.nickreuter.retroapi.share.authentication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class ShareTokenAuthentication implements Authentication {
    
    private final String token;
    private final UUID retroId;
    private final boolean authenticated;
    private final String name;
    
    public ShareTokenAuthentication(String token, UUID retroId, boolean authenticated, String name) {
        this.token = token;
        this.retroId = retroId;
        this.authenticated = authenticated;
        this.name = name;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_SHARE_TOKEN_USER"));
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
    
    @Override
    public Object getDetails() {
        return new ShareTokenUserDetails(retroId);
    }
    
    @Override
    public Object getPrincipal() {
        return "share_token_user_" + retroId;
    }
    
    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        // Immutable - throw exception
        throw new IllegalArgumentException("Cannot set authenticated on ShareTokenAuthentication");
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public UUID getRetroId() {
        return retroId;
    }
    
    public static class ShareTokenUserDetails {
        private final UUID retroId;
        
        public ShareTokenUserDetails(UUID retroId) {
            this.retroId = retroId;
        }
        
        public UUID getRetroId() {
            return retroId;
        }
    }
}
