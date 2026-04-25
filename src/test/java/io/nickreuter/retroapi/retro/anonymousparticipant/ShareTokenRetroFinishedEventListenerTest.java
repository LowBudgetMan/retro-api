package io.nickreuter.retroapi.retro.anonymousparticipant;

import io.nickreuter.retroapi.notification.event.RetroFinishedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.mockito.Mockito.*;

@Service
class ShareTokenRetroFinishedEventListenerTest {
    private final ShareTokenService mockShareTokenService = mock(ShareTokenService.class);
    private final ShareTokenRetroFinishedEventListener subject = new ShareTokenRetroFinishedEventListener(mockShareTokenService);

    @Test
    void onApplicationEvent_ShouldCallClearShareTokensForRetro() {
        var retroId = UUID.randomUUID();
        var expectedEvent = new RetroFinishedEvent("", true, retroId);
        subject.onApplicationEvent(expectedEvent);
        verify(mockShareTokenService).clearShareTokensForRetro(retroId);
    }

    @Test
    void onApplicationEvent_WhenRetroIsNotFinished_ShouldNotCallClearShareTokensForRetro() {
        var retroId = UUID.randomUUID();
        var expectedEvent = new RetroFinishedEvent("", false, retroId);
        subject.onApplicationEvent(expectedEvent);
        verifyNoInteractions(mockShareTokenService);
    }
}