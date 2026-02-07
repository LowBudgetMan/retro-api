package io.nickreuter.retroapi.share;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ShareToken(
    UUID id,
    String token,
    UUID retroId,
    UUID createdBy,
    Instant createdAt,
    Instant expiresAt,
    Integer maxUses,
    Integer uses,
    boolean active
) {
    
    public static ShareToken fromEntity(ShareTokenEntity entity) {
        return new ShareToken(
            entity.getId(),
            entity.getToken(),
            entity.getRetroId(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getExpiresAt(),
            entity.getMaxUses(),
            entity.getUses(),
            entity.isActive()
        );
    }
    
    public ShareToken withToken(String newToken) {
        return new ShareToken(
            id,
            newToken,
            retroId,
            createdBy,
            createdAt,
            expiresAt,
            maxUses,
            uses,
            active
        );
    }
}
