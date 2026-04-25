package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.notification.event.RetroFinishedEvent;
import io.nickreuter.retroapi.retro.Retro;
import io.nickreuter.retroapi.retro.RetroService;
import io.nickreuter.retroapi.retro.template.Template;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class ActionItemRetroFinishedEventListenerTest {
    private final RetroService mockRetroService = mock(RetroService.class);
    private final ActionItemService mockActionItemService = mock(ActionItemService.class);
    private final ActionItemRetroFinishedEventListener subject = new ActionItemRetroFinishedEventListener(mockRetroService, mockActionItemService);

    @Test
    void onApplicationEvent_ShouldCallClearShareTokensForRetro() {
        var expectedRetro = new Retro(UUID.randomUUID(), UUID.randomUUID(), true, new Template("id", "name", "description", List.of()), new HashSet<>(), Instant.now());
        var expectedEvent = new RetroFinishedEvent("", true, expectedRetro.id());
        when(mockRetroService.getRetro(expectedRetro.id())).thenReturn(Optional.of(expectedRetro));
        subject.onApplicationEvent(expectedEvent);
        verify(mockActionItemService).archiveCompletedActionItems(expectedRetro.teamId());
    }

    @Test
    void onApplicationEvent_WhenRetroIsNotFinished_ShouldNotCallClearShareTokensForRetro() {
        var expectedRetro = new Retro(UUID.randomUUID(), UUID.randomUUID(), true, new Template("id", "name", "description", List.of()), new HashSet<>(), Instant.now());
        var expectedEvent = new RetroFinishedEvent("", false, expectedRetro.id());
        when(mockRetroService.getRetro(expectedRetro.id())).thenReturn(Optional.of(expectedRetro));
        subject.onApplicationEvent(expectedEvent);
        verifyNoInteractions(mockActionItemService);
    }
}