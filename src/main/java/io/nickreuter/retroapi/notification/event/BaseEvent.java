package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.ActionType;
import org.springframework.context.ApplicationEvent;

public abstract class BaseEvent extends ApplicationEvent {
    private final ActionType actionType;
    private final Object payload;

    public BaseEvent(Object source, ActionType actionType, Object payload) {
        super(source);
        this.actionType = actionType;
        this.payload = payload;
    }

    public abstract String getRoute();

    public ActionType getActionType() {
        return this.actionType;
    }

    public Object getPayload() {
        return this.payload;
    }
}
