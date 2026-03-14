package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.EventType;

import java.util.UUID;

public class RetroFinishedEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/retros.%s.events";
    private final UUID retroId;

    public RetroFinishedEvent(Object source, boolean isFinished, UUID retroId) {
        super(source, EventType.RETRO_FINISHED, isFinished);
        this.retroId = retroId;
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, retroId);
    }
}
