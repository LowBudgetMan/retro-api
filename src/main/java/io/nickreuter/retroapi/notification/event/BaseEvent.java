package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.EventType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class BaseEvent extends ApplicationEvent {
    private final EventType eventType;
    private final Object payload;

    public BaseEvent(Object source, EventType eventType, Object payload) {
        super(source);
        this.eventType = eventType;
        this.payload = payload;
    }

    public abstract String getRoute();
}
