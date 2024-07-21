package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.ActionType;

import java.util.UUID;

public class RetroFinishedEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/%s.finished";
    private final UUID retroId;

    public RetroFinishedEvent(Object source, ActionType actionType, boolean isFinished, UUID retroId) {
        super(source, actionType, isFinished);
        this.retroId = retroId;
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, retroId);
    }
}
