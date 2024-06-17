package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.template.Template;
import io.nickreuter.retroapi.retro.thought.ThoughtEntity;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Retro(
    UUID id,
    UUID teamId,
    boolean finished,
    Template template,
    Set<ThoughtEntity> thoughts,
    Instant createdAt
) {
    public static Retro from(RetroEntity retroEntity, Template template) {
        return new Retro(
                retroEntity.getId(),
                retroEntity.getTeamId(),
                retroEntity.isFinished(),
                template,
                retroEntity.getThoughts(),
                retroEntity.getCreatedAt()
        );
    }
}
