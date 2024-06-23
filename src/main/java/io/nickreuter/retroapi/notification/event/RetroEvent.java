package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.ActionType;

import java.util.UUID;

public class RetroEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/%s/thoughts";
    private final UUID retroId;

    public RetroEvent(Object source, ActionType actionType, Object payload, UUID retroId) {
        super(source, actionType, payload);
        this.retroId = retroId;
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, retroId);
    }
}
