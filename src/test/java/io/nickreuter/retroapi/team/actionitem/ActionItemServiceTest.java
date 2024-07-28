package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.ActionItemEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
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
        when(actionItemRepository.save(argThat((entity) ->
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

    @Test
    void setAction_SavesActionItemInRepository() {
        var id = UUID.randomUUID();
        var action = "This is an action";
        var savedActionItem = new ActionItemEntity(id, "old action", false, UUID.randomUUID(), "assignee", Instant.now());
        var expected = new ActionItemEntity(id, action, false, savedActionItem.getTeamId(), "assignee", savedActionItem.getCreatedAt());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));
        when(actionItemRepository.save(any())).thenReturn(expected);

        subject.setAction(id, action);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEntity.class);
        verify(actionItemRepository).save(argCaptor.capture());
        assertThat(argCaptor.getValue().getId()).isEqualTo(expected.getId());
        assertThat(argCaptor.getValue().getAction()).isEqualTo(expected.getAction());
        assertThat(argCaptor.getValue().isCompleted()).isEqualTo(expected.isCompleted());
        assertThat(argCaptor.getValue().getTeamId()).isEqualTo(expected.getTeamId());
        assertThat(argCaptor.getValue().getAssignee()).isEqualTo(expected.getAssignee());
        assertThat(argCaptor.getValue().getCreatedAt()).isEqualTo(expected.getCreatedAt());
    }

    @Test
    void setAction_PublishesEvent() {
        var id = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var action = "This is an action";
        var savedActionItem = new ActionItemEntity(id, "old action", false, teamId, "assignee", Instant.now());
        var expected = new ActionItemEntity(id, action, false, savedActionItem.getTeamId(), "assignee", savedActionItem.getCreatedAt());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));
        when(actionItemRepository.save(any())).thenReturn(expected);

        subject.setAction(id, action);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/%s.action-items".formatted(teamId));
        assertThat(argCaptor.getValue().getActionType()).isEqualTo(ActionType.UPDATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void setAssignee_SavesActionItemInRepository() {
        var id = UUID.randomUUID();
        var assignee = "new assignee";
        var savedActionItem = new ActionItemEntity(id, "action", false, UUID.randomUUID(), "old assignee", Instant.now());
        var expected = new ActionItemEntity(id, "action", false, savedActionItem.getTeamId(), assignee, savedActionItem.getCreatedAt());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));
        when(actionItemRepository.save(any())).thenReturn(expected);

        subject.setAssignee(id, assignee);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEntity.class);
        verify(actionItemRepository).save(argCaptor.capture());
        assertThat(argCaptor.getValue().getId()).isEqualTo(expected.getId());
        assertThat(argCaptor.getValue().getAction()).isEqualTo(expected.getAction());
        assertThat(argCaptor.getValue().isCompleted()).isEqualTo(expected.isCompleted());
        assertThat(argCaptor.getValue().getTeamId()).isEqualTo(expected.getTeamId());
        assertThat(argCaptor.getValue().getAssignee()).isEqualTo(expected.getAssignee());
        assertThat(argCaptor.getValue().getCreatedAt()).isEqualTo(expected.getCreatedAt());
    }

    @Test
    void setAssignee_PublishesEvent() {
        var id = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var assignee = "new assignee";
        var savedActionItem = new ActionItemEntity(id, "action", false, teamId, "old assignee", Instant.now());
        var expected = new ActionItemEntity(id, "action", false, savedActionItem.getTeamId(), assignee, savedActionItem.getCreatedAt());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));
        when(actionItemRepository.save(any())).thenReturn(expected);

        subject.setAssignee(id, assignee);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/%s.action-items".formatted(teamId));
        assertThat(argCaptor.getValue().getActionType()).isEqualTo(ActionType.UPDATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void setCompleted_SavesActionItemInRepository() {
        var id = UUID.randomUUID();
        var completed = true;
        var savedActionItem = new ActionItemEntity(id, "action", false, UUID.randomUUID(), "assignee", Instant.now());
        var expected = new ActionItemEntity(id, "action", completed, savedActionItem.getTeamId(), "assignee", savedActionItem.getCreatedAt());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));
        when(actionItemRepository.save(any())).thenReturn(expected);

        subject.setCompleted(id, completed);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEntity.class);
        verify(actionItemRepository).save(argCaptor.capture());
        assertThat(argCaptor.getValue().getId()).isEqualTo(expected.getId());
        assertThat(argCaptor.getValue().getAction()).isEqualTo(expected.getAction());
        assertThat(argCaptor.getValue().isCompleted()).isEqualTo(expected.isCompleted());
        assertThat(argCaptor.getValue().getTeamId()).isEqualTo(expected.getTeamId());
        assertThat(argCaptor.getValue().getAssignee()).isEqualTo(expected.getAssignee());
        assertThat(argCaptor.getValue().getCreatedAt()).isEqualTo(expected.getCreatedAt());
    }

    @Test
    void setCompleted_PublishesEvent() {
        var id = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var completed = true;
        var savedActionItem = new ActionItemEntity(id, "action", false, teamId, "assignee", Instant.now());
        var expected = new ActionItemEntity(id, "action", completed, savedActionItem.getTeamId(), "assignee", savedActionItem.getCreatedAt());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));
        when(actionItemRepository.save(any())).thenReturn(expected);

        subject.setCompleted(id, completed);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/%s.action-items".formatted(teamId));
        assertThat(argCaptor.getValue().getActionType()).isEqualTo(ActionType.UPDATE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(expected);
    }

    @Test
    void deleteActionItem_RemovesActionItemInRepository() {
        var id = UUID.randomUUID();
        var savedActionItem = new ActionItemEntity(id, "action", false, UUID.randomUUID(), "assignee", Instant.now());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));

        subject.deleteActionItem(id);

        verify(actionItemRepository).delete(savedActionItem);
    }

    @Test
    void deleteActionItem_PublishesEvent() {
        var id = UUID.randomUUID();
        var teamId = UUID.randomUUID();
        var savedActionItem = new ActionItemEntity(id, "action", false, teamId, "assignee", Instant.now());
        when(actionItemRepository.findById(id)).thenReturn(Optional.of(savedActionItem));

        subject.deleteActionItem(id);

        var argCaptor = ArgumentCaptor.forClass(ActionItemEvent.class);
        verify(applicationEventPublisher).publishEvent(argCaptor.capture());
        assertThat(argCaptor.getValue().getRoute()).isEqualTo("/topic/%s.action-items".formatted(teamId));
        assertThat(argCaptor.getValue().getActionType()).isEqualTo(ActionType.DELETE);
        assertThat(argCaptor.getValue().getPayload()).isEqualTo(savedActionItem);
    }
}