package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.retro.thought.ThoughtEntity;

import java.util.UUID;

public class ThoughtEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/retros.%s.thoughts";
    private final UUID retroId;
    private final UUID teamId;

    public ThoughtEvent(Object source, EventType eventType, ThoughtEntity payload, UUID retroId, UUID teamId) {
        super(source, eventType, payload);
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
