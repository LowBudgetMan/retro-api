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

    public void publishTimerStart(UUID retroId, int durationSeconds, UUID teamId) {
        var payload = Map.of("durationSeconds", durationSeconds, "startedAt", Instant.now().toString());
        applicationEventPublisher.publishEvent(new RetroTimerStartEvent(this, payload, retroId, teamId));
    }

    public void publishTimerStop(UUID retroId, UUID teamId) {
        applicationEventPublisher.publishEvent(new RetroTimerStopEvent(this, null, retroId, teamId));
    }

    public void publishFocus(UUID retroId, UUID thoughtId, UUID teamId) {
        var payload = Map.of("thoughtId", thoughtId.toString());
        applicationEventPublisher.publishEvent(new RetroFocusEvent(this, payload, retroId, teamId));
    }

    public void publishFocusClear(UUID retroId, UUID teamId) {
        applicationEventPublisher.publishEvent(new RetroFocusClearEvent(this, null, retroId, teamId));
    }

    public void publishSort(UUID retroId, String column, String direction, UUID teamId) {
        var payload = Map.of("column", column, "direction", direction);
        applicationEventPublisher.publishEvent(new RetroSortEvent(this, payload, retroId, teamId));
    }

    public void publishPhase(UUID retroId, String phase, UUID teamId) {
        var payload = Map.of("phase", phase);
        applicationEventPublisher.publishEvent(new RetroPhaseEvent(this, payload, retroId, teamId));
    }
}
