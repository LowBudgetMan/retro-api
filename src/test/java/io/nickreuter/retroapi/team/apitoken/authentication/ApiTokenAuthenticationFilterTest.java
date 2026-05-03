package io.nickreuter.retroapi.team.apitoken.authentication;

import io.nickreuter.retroapi.team.apitoken.ApiTokenEntity;
import io.nickreuter.retroapi.team.apitoken.ApiTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApiTokenAuthenticationFilterTest {
    private final ApiTokenService apiTokenService = mock(ApiTokenService.class);
    private final ApiTokenAuthenticationFilter subject = new ApiTokenAuthenticationFilter(apiTokenService);

    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void doFilter_WithValidApiToken_SetsAuthentication() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        var token = "retro_pat_validtokenvalue";
        var tokenId = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var entity = new ApiTokenEntity(tokenId, teamId, "n", "h", "p", "read,write", Instant.now(), "user-1", null, null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(apiTokenService.findByTokenString(token)).thenReturn(Optional.of(entity));

        subject.doFilter(request, response, chain);

        var auth = (ApiTokenAuthentication) SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getTokenId()).isEqualTo(tokenId);
        assertThat(auth.getTeamId()).isEqualTo(teamId);
        assertThat(auth.getScopes()).containsExactlyInAnyOrder("read", "write");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_WithValidApiToken_TouchesLastUsed() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        var token = "retro_pat_validtokenvalue";
        var tokenId = UUID.randomUUID();
        var entity = new ApiTokenEntity(tokenId, UUID.randomUUID(), "n", "h", "p", "read", Instant.now(), "user-1", null, null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(apiTokenService.findByTokenString(token)).thenReturn(Optional.of(entity));

        subject.doFilter(request, response, chain);

        verify(apiTokenService).touchLastUsed(tokenId);
    }

    @Test
    void doFilter_WithUnknownApiToken_DoesNotSetAuthentication() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer retro_pat_unknown1234567890");
        when(apiTokenService.findByTokenString(any())).thenReturn(Optional.empty());

        subject.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_WithJwtBearer_DoesNotConsume() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJIUzI1NiJ9.fake.jwt");

        subject.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(apiTokenService);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_WithNoAuthHeader_PassesThrough() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        subject.doFilter(request, response, chain);

        verifyNoInteractions(apiTokenService);
        verify(chain).doFilter(request, response);
    }
}
