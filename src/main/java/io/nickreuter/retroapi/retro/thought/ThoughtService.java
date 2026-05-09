package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.notification.event.ThoughtEvent;
import io.nickreuter.retroapi.retro.RetroRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ThoughtService {
    private final ThoughtRepository thoughtRepository;
    private final RetroRepository retroRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ThoughtService(ThoughtRepository thoughtRepository, RetroRepository retroRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.thoughtRepository = thoughtRepository;
        this.retroRepository = retroRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public ThoughtEntity createThought(UUID retroId, String message, String category) {
        var savedThought = thoughtRepository.save(ThoughtEntity.from(message, category, retroId));
        var teamId = retroRepository.findById(retroId).orElseThrow().getTeamId();
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, EventType.CREATE, savedThought, retroId, teamId));
        return savedThought;
    }

    public Optional<ThoughtEntity> getThought(UUID thoughtId) {
        return thoughtRepository.findById(thoughtId);
    }

    public void addVote(UUID thoughtId) {
        thoughtRepository.incrementVotes(thoughtId);
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        var teamId = retroRepository.findById(thought.getRetroId()).orElseThrow().getTeamId();
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, EventType.UPDATE, thought, thought.getRetroId(), teamId));
    }

    public void setCompleted(UUID thoughtId, boolean completed) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thought.setCompleted(completed);
        var updatedThought = thoughtRepository.save(thought);
        var teamId = retroRepository.findById(updatedThought.getRetroId()).orElseThrow().getTeamId();
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, EventType.UPDATE, updatedThought, updatedThought.getRetroId(), teamId));
    }

    public void setCategory(UUID thoughtId, String category) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thought.setCategory(category);
        var updatedThought = thoughtRepository.save(thought);
        var teamId = retroRepository.findById(updatedThought.getRetroId()).orElseThrow().getTeamId();
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, EventType.UPDATE, updatedThought, updatedThought.getRetroId(), teamId));
    }

    public void setMessage(UUID thoughtId, String message) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thought.setMessage(message);
        var updatedThought = thoughtRepository.save(thought);
        var teamId = retroRepository.findById(updatedThought.getRetroId()).orElseThrow().getTeamId();
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, EventType.UPDATE, updatedThought, updatedThought.getRetroId(), teamId));
    }

    public void deleteThought(UUID thoughtId) {
        var thought = thoughtRepository.findById(thoughtId).orElseThrow();
        thoughtRepository.deleteById(thoughtId);
        var teamId = retroRepository.findById(thought.getRetroId()).orElseThrow().getTeamId();
        applicationEventPublisher.publishEvent(new ThoughtEvent(this, EventType.DELETE, thought, thought.getRetroId(), teamId));
    }

    public List<ThoughtEntity> getThoughtsForRetro(UUID retroId) {
        return thoughtRepository.findByRetroIdOrderByCreatedAtDesc(retroId);
    }
}
