package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.ActionItemEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ActionItemService {
    private final ActionItemRepository actionItemRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ActionItemService(ActionItemRepository actionItemRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.actionItemRepository = actionItemRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public ActionItemEntity createActionItem(String action, String assignee, UUID teamId) {
        var actionItem = actionItemRepository.save(ActionItemEntity.from(action, assignee, teamId));
        applicationEventPublisher.publishEvent(new ActionItemEvent(this, ActionType.CREATE, actionItem, teamId));
        return actionItem;
    }

    public void setAction(UUID actionItemId, String action) {
        var actionItem = actionItemRepository.findById(actionItemId).orElseThrow();
        actionItem.setAction(action);
        var updatedActionItem = actionItemRepository.save(actionItem);
        applicationEventPublisher.publishEvent(new ActionItemEvent(this, ActionType.UPDATE, updatedActionItem, updatedActionItem.getTeamId()));
    }

    public void setAssignee(UUID actionItemId, String assignee) {
        var actionItem = actionItemRepository.findById(actionItemId).orElseThrow();
        actionItem.setAssignee(assignee);
        var updatedActionItem = actionItemRepository.save(actionItem);
        applicationEventPublisher.publishEvent(new ActionItemEvent(this, ActionType.UPDATE, updatedActionItem, updatedActionItem.getTeamId()));
    }

    public void setCompleted(UUID actionItemId, boolean completed) {
        var actionItem = actionItemRepository.findById(actionItemId).orElseThrow();
        actionItem.setCompleted(completed);
        var updatedActionItem = actionItemRepository.save(actionItem);
        applicationEventPublisher.publishEvent(new ActionItemEvent(this, ActionType.UPDATE, updatedActionItem, updatedActionItem.getTeamId()));
    }

    public void deleteActionItem(UUID actionItemId) {
        var actionItem = actionItemRepository.findById(actionItemId).orElseThrow();
        actionItemRepository.delete(actionItem);
        applicationEventPublisher.publishEvent(new ActionItemEvent(this, ActionType.DELETE, actionItem, actionItem.getTeamId()));
    }

    public Optional<ActionItemEntity> getActionItem(UUID actionItemId) {
        return actionItemRepository.findById(actionItemId);
    }
}
