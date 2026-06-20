package io.nickreuter.retroapi.team.webhook;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record WebhookView(
    UUID id, String name, String url, Set<String> eventTypes, Instant createdAt
) {
    public static WebhookView from(WebhookEntity entity) {
        return new WebhookView(
            entity.getId(),
            entity.getName(),
            entity.getUrl(),
            Arrays.stream(entity.getEventTypes().split(",")).map(String::trim).collect(Collectors.toSet()),
            entity.getCreatedAt()
        );
    }
}
