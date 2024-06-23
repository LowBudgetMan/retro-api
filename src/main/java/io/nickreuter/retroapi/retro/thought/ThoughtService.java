package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.ThoughtEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ThoughtService {
    private final ThoughtRepository thoughtRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ThoughtService(ThoughtRepository thoughtRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.thoughtRepository = thoughtRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public ThoughtEntity createThought(UUID retroId, String message, String category) {
        var savedRetro = thoughtRepository.save(ThoughtEntity.from(message, category, retroId));
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, ActionType.CREATE, savedRetro, retroId));
        return savedRetro;
    }

    public List<ThoughtEntity> getThoughtsForRetro(UUID retroId) {
        return thoughtRepository.findByRetroIdOrderByCreatedAtDesc(retroId);
    }
}
