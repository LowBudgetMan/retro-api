package io.nickreuter.retroapi.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.notification.event.ThoughtEvent;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.mockito.Mockito.*;

class WebsocketNotificationServiceTest {
    private final SimpMessagingTemplate simpMessagingTemplate = mock(SimpMessagingTemplate.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final WebsocketNotificationService subject = new WebsocketNotificationService(simpMessagingTemplate, objectMapper);

    @Test
    void onApplicationEvent_WhenEventEmitted_SendMessageToWebSocket() throws Exception {
        var retroId = UUID.randomUUID();
        var event = new ThoughtEvent(this, ActionType.CREATE, null, retroId);
        when(objectMapper.writeValueAsString(any())).thenReturn("value");

        subject.onApplicationEvent(event);

        verify(simpMessagingTemplate).convertAndSend("/topic/%s/thoughts".formatted(retroId), "value");
    }

    @Test
    void onApplicationEvent_WhenEventSerializationFails_DoesNotSendMessage() throws Exception {
        var retroId = UUID.randomUUID();
        var event = new ThoughtEvent(this, ActionType.CREATE, null, retroId);
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        subject.onApplicationEvent(event);

        verify(simpMessagingTemplate, times(0)).convertAndSend("/topic/%s/thoughts".formatted(retroId), "value");
    }
}