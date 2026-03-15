package io.nickreuter.retroapi.retro.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetroEventServiceTest {
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private RetroEventService retroEventService;

    @Test
    void publishTimerStart_PublishesRetroTimerStartEvent() {
        var retroId = UUID.randomUUID();
        retroEventService.publishTimerStart(retroId, 300);
        verify(applicationEventPublisher).publishEvent(argThat(event ->
                event instanceof RetroTimerStartEvent e &&
                e.getRoute().equals("/topic/retros.%s.events".formatted(retroId))
        ));
    }

    @Test
    void publishTimerStop_PublishesRetroTimerStopEvent() {
        var retroId = UUID.randomUUID();
        retroEventService.publishTimerStop(retroId);
        verify(applicationEventPublisher).publishEvent(argThat(event ->
                event instanceof RetroTimerStopEvent e &&
                e.getRoute().equals("/topic/retros.%s.events".formatted(retroId))
        ));
    }

    @Test
    void publishFocus_PublishesRetroFocusEvent() {
        var retroId = UUID.randomUUID();
        var thoughtId = UUID.randomUUID();
        retroEventService.publishFocus(retroId, thoughtId);
        verify(applicationEventPublisher).publishEvent(argThat(event ->
                event instanceof RetroFocusEvent e &&
                e.getRoute().equals("/topic/retros.%s.events".formatted(retroId))
        ));
    }

    @Test
    void publishFocusClear_PublishesRetroFocusClearEvent() {
        var retroId = UUID.randomUUID();
        retroEventService.publishFocusClear(retroId);
        verify(applicationEventPublisher).publishEvent(argThat(event ->
                event instanceof RetroFocusClearEvent e &&
                e.getRoute().equals("/topic/retros.%s.events".formatted(retroId))
        ));
    }

    @Test
    void publishSort_PublishesRetroSortEvent() {
        var retroId = UUID.randomUUID();
        retroEventService.publishSort(retroId, "votes", "desc");
        verify(applicationEventPublisher).publishEvent(argThat(event ->
                event instanceof RetroSortEvent e &&
                e.getRoute().equals("/topic/retros.%s.events".formatted(retroId))
        ));
    }

    @Test
    void publishPhase_PublishesRetroPhaseEvent() {
        var retroId = UUID.randomUUID();
        retroEventService.publishPhase(retroId, "voting");
        verify(applicationEventPublisher).publishEvent(argThat(event ->
                event instanceof RetroPhaseEvent e &&
                e.getRoute().equals("/topic/retros.%s.events".formatted(retroId))
        ));
    }
}
