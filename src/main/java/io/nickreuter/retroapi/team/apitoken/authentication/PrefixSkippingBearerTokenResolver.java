package io.nickreuter.retroapi.team.apitoken.authentication;

import io.nickreuter.retroapi.team.apitoken.ApiTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public class PrefixSkippingBearerTokenResolver implements BearerTokenResolver {
    private final DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        var token = delegate.resolve(request);
        if (token != null && token.startsWith(ApiTokenService.TOKEN_PREFIX)) {
            return null;
        }
        return token;
    }
}
