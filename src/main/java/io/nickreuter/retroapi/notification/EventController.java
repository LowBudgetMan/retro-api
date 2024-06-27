package io.nickreuter.retroapi.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/events")
public class EventController {

    private final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public SseEmitter getEvents(@PathVariable UUID teamId) {
        SseEmitter emitter = createSseEmitter(teamId);
        eventService.addEmitter(teamId, emitter);
        return emitter;
    }

    private SseEmitter createSseEmitter(UUID teamId) {
        SseEmitter emitter = new SseEmitter();
        emitter.onCompletion(() -> eventService.removeEmitter(teamId, emitter));
        emitter.onTimeout(() -> eventService.removeEmitter(teamId, emitter));
        emitter.onError((error) -> {
            eventService.removeEmitter(teamId, emitter);
            logger.error("Something went wrong with the emitter", error);
        });
        return emitter;
    }
}
