package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.retro.thought.ThoughtEntity;

import java.util.UUID;

public class ThoughtEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/%s/thoughts";
    private final UUID retroId;

    public ThoughtEvent(Object source, ActionType actionType, ThoughtEntity payload, UUID retroId) {
        super(source, actionType, payload);
        this.retroId = retroId;
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, retroId);
    }
}
