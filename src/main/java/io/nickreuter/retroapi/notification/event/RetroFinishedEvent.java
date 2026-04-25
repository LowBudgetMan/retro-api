package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.EventType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RetroFinishedEvent extends BaseEvent {
    private static final String ROUTE_STRING = "/topic/retros.%s.events";
    private final UUID retroId;

    public RetroFinishedEvent(Object source, boolean isFinished, UUID retroId) {
        super(source, EventType.RETRO_FINISHED, isFinished);
        this.retroId = retroId;
    }

    public boolean isFinished() {
        return (boolean) getPayload();
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, retroId);
    }

}
