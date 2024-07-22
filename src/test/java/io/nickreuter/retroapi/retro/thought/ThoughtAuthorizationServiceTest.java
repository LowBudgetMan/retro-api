package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThoughtAuthorizationServiceTest {
    private final Authentication authentication = mock(Authentication.class);
    private final ThoughtService thoughtService = mock(ThoughtService.class);
    private final RetroAuthorizationService retroAuthorizationService = mock(RetroAuthorizationService.class);
    private final ThoughtAuthorizationService thoughtAuthorizationService = new ThoughtAuthorizationService(thoughtService, retroAuthorizationService);

    @Test
    void canUserModifyThought_WhenThoughtDoesNotExist_ReturnsFalse() {
        var thoughtId = UUID.randomUUID();
        when(thoughtService.getThought(thoughtId)).thenReturn(Optional.empty());
        var actual = thoughtAuthorizationService.canUserModifyThought(authentication, thoughtId);
        assertThat(actual).isFalse();
    }

    @Test
    void canUserModifyThought_WhenUserNotAllowedInRetro_ReturnsFalse() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(thoughtService.getThought(thoughtId)).thenReturn(Optional.of(ThoughtEntity.from("message", "category", retroId)));
        when(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).thenReturn(false);
        var actual = thoughtAuthorizationService.canUserModifyThought(authentication, thoughtId);
        assertThat(actual).isFalse();
    }

    @Test
    void canUserModifyThought_WhenThoughtExistsAndUserAllowedInRetro_ReturnsTrue() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(thoughtService.getThought(thoughtId)).thenReturn(Optional.of(ThoughtEntity.from("message", "category", retroId)));
        when(retroAuthorizationService.isUserAllowedInRetro(authentication, retroId)).thenReturn(true);
        var actual = thoughtAuthorizationService.canUserModifyThought(authentication, thoughtId);
        assertThat(actual).isTrue();
    }
}