package io.nickreuter.retroapi.team.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.nickreuter.retroapi.notification.EventType;
import io.nickreuter.retroapi.notification.event.ActionItemEvent;
import io.nickreuter.retroapi.team.actionitem.ActionItemEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WebhookDeliveryServiceTest {
    private final WebhookService webhookService = mock(WebhookService.class);
    private final HttpClient httpClient = mock(HttpClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private WebhookDeliveryService subject;

    @BeforeEach
    void setUp() {
        subject = new WebhookDeliveryService(webhookService, httpClient, objectMapper);
    }

    @Test
    void onApplicationEvent_WithMatchingWebhook_DeliversPayload() throws Exception {
        var teamId = UUID.randomUUID();
        var webhook = new WebhookEntity(UUID.randomUUID(), teamId, "Hook", "https://example.com/hook", "secret123", "action_item.created", true, 0, null, null, null, null, "user1");
        when(webhookService.getEnabledWebhooksForTeam(teamId)).thenReturn(List.of(webhook));

        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(any(), any())).thenReturn(response);

        var actionItem = new ActionItemEntity(UUID.randomUUID(), "Do thing", false, false, teamId, "Nick", Instant.now());
        var event = new ActionItemEvent(this, EventType.CREATE, actionItem, teamId);

        subject.onApplicationEvent(event);

        verify(httpClient).send(argThat(request -> {
            var headers = request.headers();
            return request.uri().toString().equals("https://example.com/hook")
                && headers.firstValue("Content-Type").orElse("").equals("application/json")
                && headers.firstValue("X-Retro-Event").orElse("").equals("action_item.created")
                && headers.firstValue("X-Retro-Signature").orElse("").startsWith("sha256=")
                && headers.firstValue("X-Retro-Delivery").isPresent();
        }), any());
        verify(webhookService).recordSuccess(webhook.getId());
    }

    @Test
    void onApplicationEvent_WithNonMatchingEventType_DoesNotDeliver() throws Exception {
        var teamId = UUID.randomUUID();
        var webhook = new WebhookEntity(UUID.randomUUID(), teamId, "Hook", "https://example.com/hook", "secret123", "retro.finished", true, 0, null, null, null, null, "user1");
        when(webhookService.getEnabledWebhooksForTeam(teamId)).thenReturn(List.of(webhook));

        var actionItem = new ActionItemEntity(UUID.randomUUID(), "Do thing", false, false, teamId, "Nick", Instant.now());
        var event = new ActionItemEvent(this, EventType.CREATE, actionItem, teamId);

        subject.onApplicationEvent(event);

        verify(httpClient, never()).send(any(), any());
    }

    @Test
    void onApplicationEvent_WithFailedDelivery_RecordsFailure() throws Exception {
        var teamId = UUID.randomUUID();
        var webhookId = UUID.randomUUID();
        var webhook = new WebhookEntity(webhookId, teamId, "Hook", "https://example.com/hook", "secret123", "action_item.created", true, 0, null, null, null, null, "user1");
        when(webhookService.getEnabledWebhooksForTeam(teamId)).thenReturn(List.of(webhook));

        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(500);
        when(httpClient.send(any(), any())).thenReturn(response);

        var actionItem = new ActionItemEntity(UUID.randomUUID(), "Do thing", false, false, teamId, "Nick", Instant.now());
        var event = new ActionItemEvent(this, EventType.CREATE, actionItem, teamId);

        subject.onApplicationEvent(event);

        verify(webhookService).recordFailure(eq(webhookId), anyString());
    }

    @Test
    void onApplicationEvent_WithNoWebhooks_DoesNothing() throws Exception {
        var teamId = UUID.randomUUID();
        when(webhookService.getEnabledWebhooksForTeam(teamId)).thenReturn(List.of());

        var actionItem = new ActionItemEntity(UUID.randomUUID(), "Do thing", false, false, teamId, "Nick", Instant.now());
        var event = new ActionItemEvent(this, EventType.CREATE, actionItem, teamId);

        subject.onApplicationEvent(event);

        verify(httpClient, never()).send(any(), any());
    }

    @Test
    void onApplicationEvent_WithUnmappedEventType_DoesNotDeliver() throws Exception {
        var teamId = UUID.randomUUID();
        var webhook = new WebhookEntity(UUID.randomUUID(), teamId, "Hook", "https://example.com/hook", "secret123", "action_item.created", true, 0, null, null, null, null, "user1");
        when(webhookService.getEnabledWebhooksForTeam(teamId)).thenReturn(List.of(webhook));

        var actionItem = new ActionItemEntity(UUID.randomUUID(), "Do thing", false, false, teamId, "Nick", Instant.now());
        var event = new ActionItemEvent(this, EventType.TIMER_START, actionItem, teamId);

        subject.onApplicationEvent(event);

        verify(httpClient, never()).send(any(), any());
    }
}
