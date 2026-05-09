package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.notification.event.ThoughtEvent;
import io.nickreuter.retroapi.retro.RetroEntity;
import io.nickreuter.retroapi.retro.RetroRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ThoughtServiceTest {
    private final ThoughtRepository thoughtRepository = mock(ThoughtRepository.class);
    private final RetroRepository retroRepository = mock(RetroRepository.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final ThoughtService subject = new ThoughtService(thoughtRepository, retroRepository, applicationEventPublisher);

    @Test
    void createThought_SavesThoughtInRepository() {
        var message = "message";
        var category = "category";
        var retroId = UUID.randomUUID();
        var expected = ThoughtEntity.from(message, category, retroId);
        when(thoughtRepository.save(ArgumentMatchers.argThat(entity ->
                entity.getId() == null &&
                Objects.equals(entity.getMessage(), message) &&
                entity.getVotes() == 0 &&
                !entity.isCompleted() &&
                Objects.equals(entity.getCategory(), category) &&
                entity.getRetroId() == retroId &&
                entity.getCreatedAt() == null))
        ).thenReturn(expected);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        var actual = subject.createThought(retroId, message, category);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createThought_SendsEventToApplicationEventPublisher() {
        var message = "message";
        var category = "category";
        var retroId = UUID.randomUUID();
        var expected = ThoughtEntity.from(message, category, retroId);
        when(thoughtRepository.save(ArgumentMatchers.argThat(entity ->
                entity.getId() == null &&
                        Objects.equals(entity.getMessage(), message) &&
                        entity.getVotes() == 0 &&
                        !entity.isCompleted() &&
                        Objects.equals(entity.getCategory(), category) &&
                        entity.getRetroId() == retroId &&
                        entity.getCreatedAt() == null))
        ).thenReturn(expected);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.createThought(retroId, message, category);

        var argCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/retros.%s.thoughts".formatted(retroId));
        assertThat(argCaptor.getValue().getEventType()).isEqualTo(EventType.CREATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void getThought_ReturnsValueFromRepository() {
        var thoughtId = UUID.randomUUID();
        var expected = ThoughtEntity.from("message", "category", thoughtId);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(expected));
        var actual = subject.getThought(thoughtId);
        assertThat(actual).contains(expected);
    }

    @Test
    void getThoughtsForRetro_ReturnsListFromDatabase() {
        var retroId = UUID.randomUUID();
        var expected = List.of(new ThoughtEntity(null, "message", 0, false, "category", retroId, null));
        when(thoughtRepository.findByRetroIdOrderByCreatedAtDesc(retroId)).thenReturn(expected);

        var actual = subject.getThoughtsForRetro(retroId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void addVote_UpdatesVoteCount() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(new ThoughtEntity(thoughtId, null, 2, false, "category", retroId, null)));
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));
        subject.addVote(thoughtId);
        verify(thoughtRepository).incrementVotes(thoughtId);
    }

    @Test
    void addVote_WhenCountUpdated_PublishesEvent() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var expected = new ThoughtEntity(thoughtId, null, 2, false, "category", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(expected));
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.addVote(thoughtId);

        var argCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/retros.%s.thoughts".formatted(expected.getRetroId()));
        assertThat(argCaptor.getValue().getEventType()).isEqualTo(EventType.UPDATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void setCompleted_SavedTheUpdatedCompleted() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, null, 2, false, "category", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(thoughtRepository.save(any())).thenReturn(savedThought);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.setCompleted(thoughtId, true);

        var argCaptor = ArgumentCaptor.forClass(ThoughtEntity.class);
        verify(thoughtRepository).save(argCaptor.capture());
        assertThat(argCaptor.getValue().isCompleted()).isTrue();
    }

    @Test
    void setCompleted_PublishesEvent() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, null, 2, false, "category", retroId, null);
        var expected = new ThoughtEntity(thoughtId, null, 2, true, "category", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(thoughtRepository.save(any())).thenReturn(expected);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.setCompleted(thoughtId, true);

        var argCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/retros.%s.thoughts".formatted(expected.getRetroId()));
        assertThat(argCaptor.getValue().getEventType()).isEqualTo(EventType.UPDATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void setCategory_SavesUpdatedCategoryToDatabase() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, null, 2, false, "category", retroId, null);
        var expected = new ThoughtEntity(thoughtId, null, 2, true, "category2", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(thoughtRepository.save(any())).thenReturn(expected);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.setCategory(thoughtId, "category2");

        var argCaptor = ArgumentCaptor.forClass(ThoughtEntity.class);
        verify(thoughtRepository).save(argCaptor.capture());
        assertThat(argCaptor.getValue().getCategory()).isEqualTo("category2");
    }

    @Test
    void setCategory_PublishesEvent() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, null, 2, false, "category", retroId, null);
        var expected = new ThoughtEntity(thoughtId, null, 2, false, "category2", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(thoughtRepository.save(any())).thenReturn(expected);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.setCategory(thoughtId, "category2");

        var argCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/retros.%s.thoughts".formatted(expected.getRetroId()));
        assertThat(argCaptor.getValue().getEventType()).isEqualTo(EventType.UPDATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void setMessage_SavesUpdatedMessageToDatabase() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, "message", 2, false, "category", retroId, null);
        var expected = new ThoughtEntity(thoughtId, "message 2", 2, false, "category", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(thoughtRepository.save(any())).thenReturn(expected);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.setMessage(thoughtId, "message 2");

        var argCaptor = ArgumentCaptor.forClass(ThoughtEntity.class);
        verify(thoughtRepository).save(argCaptor.capture());
        assertThat(argCaptor.getValue().getMessage()).isEqualTo("message 2");
    }

    @Test
    void setMessage_PublishesEvent() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, "message", 2, false, "category", retroId, null);
        var expected = new ThoughtEntity(thoughtId, "message 2", 2, false, "category", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(thoughtRepository.save(any())).thenReturn(expected);
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.setMessage(thoughtId, "message 2");

        var argCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/retros.%s.thoughts".formatted(expected.getRetroId()));
        assertThat(argCaptor.getValue().getEventType()).isEqualTo(EventType.UPDATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void deleteThought_RemovesThoughtFromDatabase() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, "message", 2, false, "category", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.deleteThought(thoughtId);

        verify(thoughtRepository).deleteById(thoughtId);
    }

    @Test
    void deleteThought_PublishesEvent() {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var savedThought = new ThoughtEntity(thoughtId, "message", 2, false, "category", retroId, null);
        when(thoughtRepository.findById(thoughtId)).thenReturn(Optional.of(savedThought));
        when(retroRepository.findById(retroId)).thenReturn(Optional.of(new RetroEntity(UUID.randomUUID(), UUID.randomUUID(), false, "template", Set.of(), Instant.now())));

        subject.deleteThought(thoughtId);

        var argCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/retros.%s.thoughts".formatted(savedThought.getRetroId()));
        assertThat(argCaptor.getValue().getEventType()).isEqualTo(EventType.DELETE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(savedThought);
    }
}
