package io.nickreuter.retroapi.share.authentication;

import io.nickreuter.retroapi.retro.anonymousparticipant.ShareToken;
import io.nickreuter.retroapi.retro.anonymousparticipant.ShareTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShareTokenAuthenticationFilterTest {
    private final ShareTokenService shareTokenService = mock(ShareTokenService.class);
    private final ShareTokenAuthenticationFilter subject = new ShareTokenAuthenticationFilter(shareTokenService);
    private final FilterChain filterChain = mock(FilterChain.class);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidShareToken_SetsAuthentication() throws ServletException, IOException {
        var retroId = UUID.randomUUID();
        var token = "valid-token";
        var shareToken = new ShareToken(UUID.randomUUID(), token, retroId);
        when(shareTokenService.getShareToken(token)).thenReturn(Optional.of(shareToken));

        var request = new MockHttpServletRequest();
        request.addHeader("X-Share-Token", token);
        var response = new MockHttpServletResponse();

        subject.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(ShareTokenAuthentication.class);
        assertThat(((ShareTokenAuthentication) auth).getRetroId()).isEqualTo(retroId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidShareToken_DoesNotSetAuthentication() throws ServletException, IOException {
        when(shareTokenService.getShareToken("invalid")).thenReturn(Optional.empty());

        var request = new MockHttpServletRequest();
        request.addHeader("X-Share-Token", "invalid");
        var response = new MockHttpServletResponse();

        subject.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        subject.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(shareTokenService);
    }

    @Test
    void doFilterInternal_WithExistingAuthentication_DoesNotOverride() throws ServletException, IOException {
        var existingAuth = mock(JwtAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        var request = new MockHttpServletRequest();
        request.addHeader("X-Share-Token", "some-token");
        var response = new MockHttpServletResponse();

        subject.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(shareTokenService);
    }
}
