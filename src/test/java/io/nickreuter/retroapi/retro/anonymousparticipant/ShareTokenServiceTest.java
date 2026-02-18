package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShareTokenServiceTest {
    private final ShareTokenRepository mockShareTokenRepository = mock(ShareTokenRepository.class);
    private final SecureRandom mockSecureRandom = mock(SecureRandom.class);
    private final ShareTokenService subject = new ShareTokenService(mockShareTokenRepository, mockSecureRandom);

    @Test
    void createShareToken_ReturnsObjectWithSecureStringIdAndRetroId() {
        var retroId = UUID.randomUUID();
        var expected = new ShareTokenEntity(UUID.randomUUID(), "AAABAQEBAQEBAAAAAAAAAAAAAAAAAAAAAAEBAQEBAQEBAQEBAQEBAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", retroId);
        doAnswer((Answer<Void>) invocation -> {
            var expectedByteArray = new byte[]{0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
            byte[] argArray = invocation.getArgument(0);
            System.arraycopy(expectedByteArray, 0, argArray, 0, expectedByteArray.length);
            return null;
        }).when(mockSecureRandom).nextBytes(any(byte[].class));
        when(mockShareTokenRepository.save(new ShareTokenEntity(null,"AAABAQEBAQEBAAAAAAAAAAAAAAAAAAAAAAEBAQEBAQEBAQEBAQEBAQEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", retroId))).thenReturn(expected);

        var actual = subject.createShareToken(retroId);

        assertThat(actual.id()).isEqualTo(expected.getId());
        assertThat(actual.token()).isEqualTo(expected.getToken());
        assertThat(actual.retroId()).isEqualTo(expected.getRetroId());
    }

    @Test
    void isTokenValid_WhenTokenExistsInRepo_ReturnsTrue() {
        var token = "This is a token";
        when(mockShareTokenRepository.findByToken(token)).thenReturn(Optional.of(new ShareTokenEntity()));
        assertThat(subject.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_WhenTokenDoesNotExistInRepo_ReturnsFalse() {
        var token = "This is a token";
        when(mockShareTokenRepository.findByToken(token)).thenReturn(Optional.empty());
        assertThat(subject.isTokenValid(token)).isFalse();
    }
}