package io.nickreuter.retroapi.retro;

import java.time.Instant;
import java.util.UUID;

public record RetroListItem(
        UUID id,
        UUID teamId,
        Instant createdAt
) {}
