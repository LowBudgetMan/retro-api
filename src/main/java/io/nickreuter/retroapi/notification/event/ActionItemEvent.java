package io.nickreuter.retroapi.notification.event;

import io.nickreuter.retroapi.notification.ActionType;
import io.nickreuter.retroapi.team.actionitem.ActionItemEntity;

import java.util.UUID;

public class ActionItemEvent extends BaseEvent{
    private static final String ROUTE_STRING = "/topic/%s.action-items";
    private final UUID teamId;

    public ActionItemEvent(Object source, ActionType actionType, ActionItemEntity payload, UUID teamId) {
        super(source, actionType, payload);
        this.teamId = teamId;
    }

    @Override
    public String getRoute() {
        return String.format(ROUTE_STRING, teamId);
    }
}
