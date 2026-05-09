package io.nickreuter.retroapi.retro.event;

import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.notification.event.BaseEvent;

import java.util.UUID;

public class RetroFocusEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/retros.%s.events";
    private final UUID retroId;
    private final UUID teamId;

    public RetroFocusEvent(Object source, Object payload, UUID retroId, UUID teamId) {
        super(source, EventType.FOCUS, payload);
        this.retroId = retroId;
        this.teamId = teamId;
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, retroId);
    }

    @Override
    public UUID getTeamId() {
        return teamId;
    }
}
