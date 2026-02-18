package io.nickreuter.retroapi.share.authentication;

//import io.nickreuter.retroapi.share.ShareTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.UUID;

public class ShareTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    private static final String SHARE_TOKEN_HEADER = "X-Share-Token";
    private static final String SHARE_TOKEN_PARAM = "share_token";
    
//    private final ShareTokenService shareTokenService;
    
    public ShareTokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        super("/api/**");
//        this.shareTokenService = shareTokenService;
        setAuthenticationManager(authenticationManager);
    }
    
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        // Only process if we have a share token
        String token = getShareToken(request);
        return token != null && !token.trim().isEmpty();
    }
    
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
//            throws IOException, ServletException {
//
//        String token = getShareToken(request);
//        if (token == null || token.trim().isEmpty()) {
//            return null;
//        }
//
//        try {
//            var shareTokenEntity = shareTokenService.validateTokenWithoutUsage(token);
//            UUID retroId = shareTokenEntity.getRetroId();
//
//            ShareTokenAuthentication authentication = new ShareTokenAuthentication(
//                token,
//                retroId,
//                true,
//                "anonymous_user_" + retroId
//            );
//
//            return authentication;
//        } catch (IllegalArgumentException e) {
//            // Invalid token - return null to continue with normal authentication flow
//            return null;
//        }
//    }
    
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
//                                          FilterChain chain, Authentication authResult) throws IOException, ServletException {
//
//        // Increment usage count for successful authentication
//        if (authResult instanceof ShareTokenAuthentication) {
//            ShareTokenAuthentication shareAuth = (ShareTokenAuthentication) authResult;
//            try {
//                var tokenEntity = shareTokenService.validateAndUseToken(shareAuth.getCredentials().toString());
//                // Update the authentication with the retro ID from the entity
//                ShareTokenAuthentication finalAuth = new ShareTokenAuthentication(
//                    shareAuth.getCredentials().toString(),
//                    tokenEntity.getRetroId(),
//                    true,
//                    shareAuth.getName()
//                );
//                SecurityContextHolder.getContext().setAuthentication(finalAuth);
//            } catch (IllegalArgumentException e) {
//                // Token became invalid during processing - don't authenticate
//                return;
//            }
//        }
//
//        super.successfulAuthentication(request, response, chain, authResult);
//    }
    
    private String getShareToken(HttpServletRequest request) {
        // Check header first
        String headerToken = request.getHeader(SHARE_TOKEN_HEADER);
        if (headerToken != null && !headerToken.trim().isEmpty()) {
            return headerToken;
        }
        
        // Check query parameter
        String paramToken = request.getParameter(SHARE_TOKEN_PARAM);
        if (paramToken != null && !paramToken.trim().isEmpty()) {
            return paramToken;
        }
        
        return null;
    }
}
