package io.nickreuter.retroapi.retro;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/retros")
public class RetroController {

    private final RetroService retroService;

    public RetroController(RetroService retroService) {
        this.retroService = retroService;
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> createRetro(@PathVariable("teamId") UUID teamId) {
        var retro = retroService.createRetro(teamId);
        return ResponseEntity.created(URI.create("/api/teams/%s/retros/%s".formatted(teamId, retro.getId()))).build();
    }

    @GetMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<List<RetroListItem>> getRetros(@PathVariable("teamId") UUID teamId) {
        return ResponseEntity.ok(retroService.getRetros(teamId)
                .stream()
                .map(retro -> new RetroListItem(retro.getId(), retro.getTeamId(), retro.getCreatedAt()))
                .toList()
        );
    }
}
