package io.nickreuter.retroapi.team;

import org.assertj.core.util.Lists;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.HashMap;

public class TestAuthenticationCreationService {
    public static Authentication createAuthentication() {
        var headers = new HashMap<String, Object>();
        headers.put("alg", "none");
        var claims = new HashMap<String, Object>();
        claims.put("sub", "user");
        claims.put("scope", "read");
        var authorities = Lists.list(new SimpleGrantedAuthority("SCOPE_read"));
        return new JwtAuthenticationToken(
                new Jwt(
                        "token",
                        null,
                        null,
                        headers,
                        claims
                ),
                authorities,
                "user"
        );
    }
}
