package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.ThoughtEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ThoughtServiceTest {
    private final ThoughtRepository thoughtRepository = mock(ThoughtRepository.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final ThoughtService subject = new ThoughtService(thoughtRepository, applicationEventPublisher);

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

        subject.createThought(retroId, message, category);

        var argCaptor = ArgumentCaptor.forClass(ThoughtEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/%s/thoughts".formatted(retroId));
        assertThat(argCaptor.getValue().getActionType()).isEqualTo(ActionType.CREATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void getThoughtsForRetro_ReturnsListFromDatabase() {
        var retroId = UUID.randomUUID();
        var expected = List.of(new ThoughtEntity(null, "message", 0, false, "category", retroId, null));
        when(thoughtRepository.findByRetroIdOrderByCreatedAtDesc(retroId)).thenReturn(expected);

        var actual = subject.getThoughtsForRetro(retroId);

        assertThat(actual).isEqualTo(expected);
    }
}