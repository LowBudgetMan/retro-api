package io.nickreuter.retroapi.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nickreuter.retroapi.notification.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebsocketNotificationService implements ApplicationListener<BaseEvent> {
    private final Logger logger = LoggerFactory.getLogger(WebsocketNotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public WebsocketNotificationService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onApplicationEvent(@NonNull BaseEvent event) {
        try {
            messagingTemplate.convertAndSend(event.getRoute(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            logger.error("Failed to send notification", e);
        }
    }
}
