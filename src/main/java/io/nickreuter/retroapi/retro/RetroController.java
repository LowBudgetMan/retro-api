package io.nickreuter.retroapi.retro;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
}
