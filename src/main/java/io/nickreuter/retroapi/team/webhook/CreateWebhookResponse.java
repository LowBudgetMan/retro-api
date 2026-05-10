package io.nickreuter.retroapi.team.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.UUID;

public record CreateWebhookResponse(
    UUID id,
    String name,
    String url,
    @JsonProperty("event_types") Set<String> eventTypes,
    boolean enabled,
    String secret
) {}
