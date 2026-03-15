package io.nickreuter.retroapi.retro.event;

import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.notification.event.BaseEvent;

import java.util.UUID;

public class RetroFocusClearEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/retros.%s.events";
    private final UUID retroId;

    public RetroFocusClearEvent(Object source, Object payload, UUID retroId) {
        super(source, EventType.FOCUS_CLEAR, payload);
        this.retroId = retroId;
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, retroId);
    }
}
