package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.notification.event.RetroFinishedEvent;
import io.nickreuter.retroapi.retro.template.Template;
import io.nickreuter.retroapi.team.subscription.Feature;
import io.nickreuter.retroapi.team.subscription.FeatureGuard;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class RetroRetentionEventListenerTest {
    private final RetroService mockRetroService = mock(RetroService.class);
    private final FeatureGuard mockFeatureGuard = mock(FeatureGuard.class);
    private final RetroRetentionEventListener subject = new RetroRetentionEventListener(mockRetroService, mockFeatureGuard);

    private final Retro retro = new Retro(UUID.randomUUID(), UUID.randomUUID(), true,
            new Template("id", "name", "description", List.of()), new HashSet<>(), Instant.now());

    @Test
    void onApplicationEvent_WhenTeamLacksHistoricalRetros_DeletesRetro() {
        when(mockRetroService.getRetro(retro.id())).thenReturn(Optional.of(retro));
        when(mockFeatureGuard.hasFeature(retro.teamId(), Feature.HISTORICAL_RETROS)).thenReturn(false);

        subject.onApplicationEvent(new RetroFinishedEvent("", true, retro.id()));

        verify(mockRetroService).deleteRetro(retro.id());
    }

    @Test
    void onApplicationEvent_WhenTeamHasHistoricalRetros_DoesNotDeleteRetro() {
        when(mockRetroService.getRetro(retro.id())).thenReturn(Optional.of(retro));
        when(mockFeatureGuard.hasFeature(retro.teamId(), Feature.HISTORICAL_RETROS)).thenReturn(true);

        subject.onApplicationEvent(new RetroFinishedEvent("", true, retro.id()));

        verify(mockRetroService, never()).deleteRetro(any());
    }

    @Test
    void onApplicationEvent_WhenRetroIsNotFinished_DoesNothing() {
        subject.onApplicationEvent(new RetroFinishedEvent("", false, retro.id()));

        verify(mockRetroService, never()).deleteRetro(any());
        verifyNoInteractions(mockFeatureGuard);
    }
}
