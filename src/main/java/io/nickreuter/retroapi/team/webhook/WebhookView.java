package io.nickreuter.retroapi.team.webhook;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record WebhookView(
    UUID id,
    String name,
    String url,
    Set<String> eventTypes,
    boolean enabled,
    int consecutiveFailures,
    Instant lastDeliveryAt,
    Instant lastFailureAt,
    String lastFailureReason,
    Instant createdAt
) {
    public static WebhookView from(WebhookEntity entity) {
        return new WebhookView(
            entity.getId(),
            entity.getName(),
            entity.getUrl(),
            Arrays.stream(entity.getEventTypes().split(",")).map(String::trim).collect(Collectors.toSet()),
            entity.isEnabled(),
            entity.getConsecutiveFailures(),
            entity.getLastDeliveryAt(),
            entity.getLastFailureAt(),
            entity.getLastFailureReason(),
            entity.getCreatedAt()
        );
    }
}
