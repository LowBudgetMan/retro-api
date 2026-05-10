package io.nickreuter.retroapi.team.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.notification.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WebhookDeliveryService implements ApplicationListener<BaseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(WebhookDeliveryService.class);
    private static final Map<EventType, String> EVENT_TYPE_MAP = Map.of(
        EventType.CREATE, "action_item.created",
        EventType.UPDATE, "action_item.updated",
        EventType.DELETE, "action_item.deleted",
        EventType.RETRO_FINISHED, "retro.finished"
    );

    private final WebhookService webhookService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebhookDeliveryService(WebhookService webhookService, HttpClient httpClient, ObjectMapper objectMapper) {
        this.webhookService = webhookService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onApplicationEvent(@NonNull BaseEvent event) {
        var webhookEventType = EVENT_TYPE_MAP.get(event.getEventType());
        if (webhookEventType == null) return;

        var webhooks = webhookService.getEnabledWebhooksForTeam(event.getTeamId());
        for (var webhook : webhooks) {
            var subscribedTypes = Arrays.stream(webhook.getEventTypes().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
            if (!subscribedTypes.contains(webhookEventType)) continue;

            deliver(webhook, webhookEventType, event);
        }
    }

    private void deliver(WebhookEntity webhook, String eventType, BaseEvent event) {
        try {
            var payload = new WebhookPayload(eventType, event.getTeamId(), Instant.now(), event.getPayload());
            var body = objectMapper.writeValueAsString(payload);
            var signature = WebhookSignature.sign(body, webhook.getSecret());
            var deliveryId = UUID.randomUUID().toString();

            var request = HttpRequest.newBuilder()
                .uri(URI.create(webhook.getUrl()))
                .header("Content-Type", "application/json")
                .header("X-Retro-Event", eventType)
                .header("X-Retro-Delivery", deliveryId)
                .header("X-Retro-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(10))
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                webhookService.recordSuccess(webhook.getId());
            } else {
                webhookService.recordFailure(webhook.getId(), "HTTP " + response.statusCode());
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize webhook payload for webhook {}", webhook.getId(), e);
            webhookService.recordFailure(webhook.getId(), "Serialization error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to deliver webhook {} to {}", webhook.getId(), webhook.getUrl(), e);
            webhookService.recordFailure(webhook.getId(), e.getMessage());
        }
    }
}
