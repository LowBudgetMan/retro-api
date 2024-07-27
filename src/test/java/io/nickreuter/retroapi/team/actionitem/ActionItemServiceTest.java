package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.ActionItemEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActionItemServiceTest {
    private final ActionItemRepository actionItemRepository = mock(ActionItemRepository.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final ActionItemService subject = new ActionItemService(actionItemRepository, applicationEventPublisher);

    @Test
    void createActionItem_SavesActionItemInRepository() {
        var action = "This is an action";
        var assignee = "Assign E.";
        var teamId = UUID.randomUUID();
        var expected = new ActionItemEntity(UUID.randomUUID(), action, false, teamId, assignee, Instant.now());
        when(actionItemRepository.save(ArgumentMatchers.argThat((entity) ->
                entity.getId() == null &&
                Objects.equals(entity.getAction(), action) &&
                Objects.equals(entity.getAssignee(), assignee) &&
                Objects.equals(entity.getTeamId(), teamId) &&
                !entity.isCompleted() &&
                entity.getCreatedAt() == null)
        )).thenReturn(expected);

        var actual = subject.createActionItem(action, assignee, teamId);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createActionItem_PublishesEvent() {
        var action = "This is an action";
        var assignee = "Assign E.";
        var teamId = UUID.randomUUID();
        var expected = new ActionItemEntity(UUID.randomUUID(), action, false, teamId, assignee, Instant.now());
        when(actionItemRepository.save(any())).thenReturn(expected);

        subject.createActionItem(action, assignee, teamId);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/%s.action-items".formatted(teamId));
        assertThat(argCaptor.getValue().getActionType()).isEqualTo(ActionType.CREATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }
}