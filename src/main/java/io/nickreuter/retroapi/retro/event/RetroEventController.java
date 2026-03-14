package io.nickreuter.retroapi.retro.event;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/retros/{retroId}/events")
public class RetroEventController {
    private final RetroEventService retroEventService;

    public RetroEventController(RetroEventService retroEventService) {
        this.retroEventService = retroEventService;
    }

    @PostMapping("/timer-start")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> startTimer(@PathVariable UUID teamId, @PathVariable UUID retroId, @RequestBody TimerStartRequest request) {
        retroEventService.publishTimerStart(retroId, request.durationSeconds());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/timer-stop")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> stopTimer(@PathVariable UUID teamId, @PathVariable UUID retroId) {
        retroEventService.publishTimerStop(retroId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/focus")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> focusThought(@PathVariable UUID teamId, @PathVariable UUID retroId, @RequestBody FocusRequest request) {
        retroEventService.publishFocus(retroId, request.thoughtId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/focus-clear")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> clearFocus(@PathVariable UUID teamId, @PathVariable UUID retroId) {
        retroEventService.publishFocusClear(retroId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sort")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> sortColumn(@PathVariable UUID teamId, @PathVariable UUID retroId, @RequestBody SortRequest request) {
        retroEventService.publishSort(retroId, request.column(), request.direction());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phase")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> changePhase(@PathVariable UUID teamId, @PathVariable UUID retroId, @RequestBody PhaseRequest request) {
        retroEventService.publishPhase(retroId, request.phase());
        return ResponseEntity.ok().build();
    }
}
