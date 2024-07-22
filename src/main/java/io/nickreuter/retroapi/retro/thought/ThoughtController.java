package io.nickreuter.retroapi.retro.thought;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/retros/{retroId}/thoughts")
public class ThoughtController {
    private final ThoughtService thoughtService;

    public ThoughtController(ThoughtService thoughtService) {
        this.thoughtService = thoughtService;
    }

    @PostMapping
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public ResponseEntity<Void> createThought(@PathVariable UUID teamId, @PathVariable UUID retroId, @RequestBody CreateThoughtRequest request) {
        var savedThought = thoughtService.createThought(retroId, request.message(), request.category());
        return ResponseEntity.created(URI.create("/api/teams/%s/retros/%s/thoughts/%s".formatted(teamId, retroId, savedThought.getId()))).build();
    }

    @GetMapping
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public List<ThoughtEntity> getThoughts(@PathVariable UUID retroId) {
        return thoughtService.getThoughtsForRetro(retroId);
    }

    @PutMapping("/{thoughtId}/votes")
    @PreAuthorize("@thoughtAuthorizationService.canUserModifyThought(authentication, #thoughtId)")
    public ResponseEntity<Void> vote(@PathVariable UUID teamId, @PathVariable UUID retroId, @PathVariable UUID thoughtId) {
        thoughtService.addVote(thoughtId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{thoughtId}/completed")
    @PreAuthorize("@thoughtAuthorizationService.canUserModifyThought(authentication, #thoughtId)")
    public ResponseEntity<Void> setCompleted(@PathVariable UUID teamId, @PathVariable UUID retroId, @PathVariable UUID thoughtId, @RequestBody UpdateThoughtCompletionRequest request) {
        thoughtService.setCompleted(thoughtId, request.completed());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{thoughtId}/category")
    @PreAuthorize("@thoughtAuthorizationService.canUserModifyThought(authentication, #thoughtId)")
    public ResponseEntity<Void> setCategory(@PathVariable UUID teamId, @PathVariable UUID retroId, @PathVariable UUID thoughtId, @RequestBody UpdateThoughtCategoryRequest request) {
        thoughtService.setCategory(thoughtId, request.category());
        return ResponseEntity.noContent().build();
    }
}
