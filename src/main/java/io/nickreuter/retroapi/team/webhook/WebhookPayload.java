package io.nickreuter.retroapi.team.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record WebhookPayload(
    @JsonProperty("event_type") String eventType,
    @JsonProperty("team_id") UUID teamId,
    @JsonProperty("occurred_at") Instant occurredAt,
    Object data
) {}
