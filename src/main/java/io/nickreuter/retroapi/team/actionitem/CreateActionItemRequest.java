package io.nickreuter.retroapi.team.actionitem;

import java.util.UUID;

public record CreateActionItemRequest(
        String action,
        String assignee,
        UUID teamId
) {}
