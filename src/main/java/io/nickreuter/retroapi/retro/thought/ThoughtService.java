package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.ThoughtEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        var savedThought = thoughtRepository.save(ThoughtEntity.from(message, category, retroId));
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, ActionType.CREATE, savedThought, retroId));
        return savedThought;
    }

    public Optional<ThoughtEntity> getThought(UUID thoughtId) {
        return thoughtRepository.findById(thoughtId);
    }

    public void addVote(UUID thoughtId) {
        thoughtRepository.incrementVotes(thoughtId);
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, ActionType.UPDATE, thought, thought.getRetroId()));
    }

    public void setCompleted(UUID thoughtId, boolean completed) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thought.setCompleted(completed);
        var updatedThought = thoughtRepository.save(thought);
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, ActionType.UPDATE, updatedThought, updatedThought.getRetroId()));
    }

    public void setCategory(UUID thoughtId, String category) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thought.setCategory(category);
        var updatedThought = thoughtRepository.save(thought);
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, ActionType.UPDATE, updatedThought, updatedThought.getRetroId()));
    }

    public void setMessage(UUID thoughtId, String message) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thought.setMessage(message);
        var updatedThought = thoughtRepository.save(thought);
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, ActionType.UPDATE, updatedThought, updatedThought.getRetroId()));
    }

    public void deleteThought(UUID thoughtId) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thoughtRepository.deleteById(thoughtId);
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, ActionType.DELETE, thought, thought.getRetroId()));
    }

    public List<ThoughtEntity> getThoughtsForRetro(UUID retroId) {
        return thoughtRepository.findByRetroIdOrderByCreatedAtDesc(retroId);
    }
}
