package io.nickreuter.retroapi.team.apitoken;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApiTokenServiceTest {
    private final ApiTokenRepository repository = mock(ApiTokenRepository.class);
    private final SecureRandom random = new SecureRandom();
    private final ApiTokenService subject = new ApiTokenService(repository, random);

    @Test
    void createToken_GeneratesTokenWithExpectedPrefix() {
        var teamId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = subject.createToken(teamId, "Slack", Set.of("read"), null, "user-1");

        assertThat(result.token()).startsWith("retro_pat_");
        assertThat(result.token()).hasSize(53);
    }

    @Test
    void createToken_StoresHashNotPlaintext() {
        var teamId = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(ApiTokenEntity.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        var result = subject.createToken(teamId, "Slack", Set.of("read"), null, "user-1");

        var saved = captor.getValue();
        assertThat(saved.getTokenHash()).isNotEqualTo(result.token());
        assertThat(saved.getTokenHash()).hasSize(64);
        assertThat(saved.getTokenPrefix()).isEqualTo(result.token().substring(0, 14));
        assertThat(saved.getTeamId()).isEqualTo(teamId);
        assertThat(saved.getName()).isEqualTo("Slack");
        assertThat(saved.getScopes()).isEqualTo("read");
        assertThat(saved.getCreatedByUserId()).isEqualTo("user-1");
        assertThat(saved.getExpiresAt()).isNull();
    }

    @Test
    void createToken_WithMultipleScopes_StoresCommaSeparated() {
        var captor = ArgumentCaptor.forClass(ApiTokenEntity.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        subject.createToken(UUID.randomUUID(), "Bot", Set.of("read", "write"), null, "user-1");

        var stored = captor.getValue().getScopes();
        assertThat(stored.split(",")).containsExactlyInAnyOrder("read", "write");
    }

    @Test
    void createToken_WithUnknownScope_ThrowsInvalidScopeException() {
        assertThatThrownBy(() -> subject.createToken(UUID.randomUUID(), "X", Set.of("admin"), null, "user-1"))
            .isInstanceOf(InvalidScopeException.class);
    }

    @Test
    void createToken_WithEmptyScopes_ThrowsInvalidScopeException() {
        assertThatThrownBy(() -> subject.createToken(UUID.randomUUID(), "X", Set.of(), null, "user-1"))
            .isInstanceOf(InvalidScopeException.class);
    }

    @Test
    void findByTokenString_WhenHashMatches_ReturnsEntity() {
        var token = "retro_pat_test1234567890";
        var hash = ApiTokenService.hash(token);
        var entity = new ApiTokenEntity(UUID.randomUUID(), UUID.randomUUID(), "n", hash, "retro_pat_test", "read", Instant.now(), "user-1", null, null);
        when(repository.findByTokenHash(hash)).thenReturn(Optional.of(entity));

        assertThat(subject.findByTokenString(token)).contains(entity);
    }

    @Test
    void findByTokenString_WhenExpired_ReturnsEmpty() {
        var token = "retro_pat_test1234567890";
        var hash = ApiTokenService.hash(token);
        var entity = new ApiTokenEntity(UUID.randomUUID(), UUID.randomUUID(), "n", hash, "retro_pat_test", "read", Instant.now(), "user-1", Instant.now().minusSeconds(60), null);
        when(repository.findByTokenHash(hash)).thenReturn(Optional.of(entity));

        assertThat(subject.findByTokenString(token)).isEmpty();
    }

    @Test
    void touchLastUsed_UpdatesTimestamp() {
        var entity = new ApiTokenEntity(UUID.randomUUID(), UUID.randomUUID(), "n", "h", "p", "read", Instant.now(), "user-1", null, null);
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        subject.touchLastUsed(entity.getId());

        var captor = ArgumentCaptor.forClass(ApiTokenEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLastUsedAt()).isNotNull();
    }

    @Test
    void getTokensForTeam_DelegatesToRepository() {
        var teamId = UUID.randomUUID();
        var entity = new ApiTokenEntity();
        when(repository.findAllByTeamId(teamId)).thenReturn(List.of(entity));

        assertThat(subject.getTokensForTeam(teamId)).containsExactly(entity);
    }

    @Test
    void deleteToken_DelegatesToRepository() {
        var id = UUID.randomUUID();

        subject.deleteToken(id);

        verify(repository).deleteById(id);
    }
}
