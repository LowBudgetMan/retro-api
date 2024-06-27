package io.nickreuter.retroapi.notification;

import io.nickreuter.retroapi.notification.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EventService implements ApplicationListener<BaseEvent> {
    private final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void addEmitter(UUID teamId, SseEmitter emitter) {
        getEmittersForTeam(teamId).add(emitter);
    }

    public void removeEmitter(UUID teamId, SseEmitter emitter) {
        getEmittersForTeam(teamId).remove(emitter);
        System.out.println("Removing emitter " + emitter);
    }

    public void emitEvent(UUID teamId, BaseEvent event) {
        var failedEmitters = new ArrayList<SseEmitter>();
        // TODO: Should this be a parallel stream?
        getEmittersForTeam(teamId).forEach(emitter -> {
            try {
                emitter.send(event, MediaType.APPLICATION_JSON);
            } catch (IOException  e) {
                failedEmitters.add(emitter);
                emitter.completeWithError(e);
                logger.error("Something went wrong while emitting event", e);
            }
        });
        failedEmitters.forEach(emitter -> removeEmitter(teamId, emitter));
    }

    private CopyOnWriteArrayList<SseEmitter> getEmittersForTeam(UUID teamId) {
        emitters.putIfAbsent(teamId, new CopyOnWriteArrayList<>());
        return emitters.get(teamId);
    }

    @Override
    public void onApplicationEvent(@NonNull BaseEvent event) {
        emitEvent(event.getTeamId(), event);
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
