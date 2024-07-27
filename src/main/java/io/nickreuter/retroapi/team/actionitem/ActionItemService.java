package io.nickreuter.retroapi.team.actionitem;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.notification.event.ActionItemEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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
}
