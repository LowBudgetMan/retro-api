package io.nickreuter.retroapi.retro.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class RetroEventService {
    private final ApplicationEventPublisher applicationEventPublisher;

    public RetroEventService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishTimerStart(UUID retroId, int durationSeconds) {
        var payload = Map.of("durationSeconds", durationSeconds, "startedAt", Instant.now().toString());
        applicationEventPublisher.publishEvent(new RetroTimerStartEvent(this, payload, retroId));
    }

    public void publishTimerStop(UUID retroId) {
        applicationEventPublisher.publishEvent(new RetroTimerStopEvent(this, null, retroId));
    }

    public void publishFocus(UUID retroId, UUID thoughtId) {
        var payload = Map.of("thoughtId", thoughtId.toString());
        applicationEventPublisher.publishEvent(new RetroFocusEvent(this, payload, retroId));
    }

    public void publishFocusClear(UUID retroId) {
        applicationEventPublisher.publishEvent(new RetroFocusClearEvent(this, null, retroId));
    }

    public void publishSort(UUID retroId, String column, String direction) {
        var payload = Map.of("column", column, "direction", direction);
        applicationEventPublisher.publishEvent(new RetroSortEvent(this, payload, retroId));
    }

    public void publishPhase(UUID retroId, String phase) {
        var payload = Map.of("phase", phase);
        applicationEventPublisher.publishEvent(new RetroPhaseEvent(this, payload, retroId));
    }
}
