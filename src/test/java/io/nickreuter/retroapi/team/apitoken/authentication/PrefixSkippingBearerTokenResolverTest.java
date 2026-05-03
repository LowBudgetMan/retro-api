package io.nickreuter.retroapi.team.apitoken.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrefixSkippingBearerTokenResolverTest {
    private final PrefixSkippingBearerTokenResolver subject = new PrefixSkippingBearerTokenResolver();

    @Test
    void resolve_WithJwtBearer_ReturnsToken() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJIUzI1NiJ9.fake.jwt");

        assertThat(subject.resolve(request)).isEqualTo("eyJhbGciOiJIUzI1NiJ9.fake.jwt");
    }

    @Test
    void resolve_WithApiTokenBearer_ReturnsNull() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer retro_pat_xyz");

        assertThat(subject.resolve(request)).isNull();
    }

    @Test
    void resolve_WithNoHeader_ReturnsNull() {
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThat(subject.resolve(request)).isNull();
    }
}
