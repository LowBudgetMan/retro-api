package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.ActionType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public abstract class BaseEvent extends ApplicationEvent {
    private final UUID teamId;
    private final ActionType actionType;
    private final Object payload;

    public BaseEvent(Object source, UUID teamId, ActionType actionType, Object payload) {
        super(source);
        this.teamId = teamId;
        this.actionType = actionType;
        this.payload = payload;
    }

    public abstract String getRoute();
}
