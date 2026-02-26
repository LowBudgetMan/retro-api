package io.nickreuter.retroapi.share.authentication;

import io.nickreuter.retroapi.retro.anonymousparticipant.ShareTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ShareTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String SHARE_TOKEN_HEADER = "X-Share-Token";

    private final ShareTokenService shareTokenService;

    public ShareTokenAuthenticationFilter(ShareTokenService shareTokenService) {
        this.shareTokenService = shareTokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String shareToken = request.getHeader(SHARE_TOKEN_HEADER);
            if (shareToken != null && !shareToken.isBlank()) {
                shareTokenService.getShareToken(shareToken).ifPresent(token -> {
                    var authentication = new ShareTokenAuthentication(
                            shareToken,
                            token.retroId(),
                            "anonymous_" + token.retroId()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
