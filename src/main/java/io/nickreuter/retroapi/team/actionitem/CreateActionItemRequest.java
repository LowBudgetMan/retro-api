package io.nickreuter.retroapi.team.actionitem;

public record CreateActionItemRequest(
        String action,
        String assignee
) {}
