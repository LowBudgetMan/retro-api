package io.nickreuter.retroapi.retro;

import java.time.Instant;
import java.util.UUID;

public record RetroListItem(
        UUID id,
        UUID teamId,
        boolean finished,
        Instant createdAt
) {
    static RetroListItem from(RetroEntity retro) {
        return new RetroListItem(retro.getId(), retro.getTeamId(), retro.isFinished(), retro.getCreatedAt());
    }
}
