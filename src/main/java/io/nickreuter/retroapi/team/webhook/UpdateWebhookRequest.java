package io.nickreuter.retroapi.team.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public record UpdateWebhookRequest(
    String name,
    String url,
    @JsonProperty("event_types") Set<String> eventTypes,
    Boolean enabled
) {}
