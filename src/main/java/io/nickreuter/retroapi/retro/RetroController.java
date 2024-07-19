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
    public ResponseEntity<Void> createRetro(@PathVariable("teamId") UUID teamId, @RequestBody CreateRetroRequest request) throws InvalidTemplateIdException {
        var retro = retroService.createRetro(teamId, request.retroTemplateId());
        return ResponseEntity.created(URI.create("/api/teams/%s/retros/%s".formatted(teamId, retro.getId()))).build();
    }

    @GetMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public List<RetroListItem> getRetros(@PathVariable("teamId") UUID teamId) {
        return retroService.getRetros(teamId)
                .stream()
                .map(RetroListItem::from)
                .toList();
    }

    @GetMapping("/{retroId}")
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public Retro getRetro(@PathVariable("retroId") UUID retroId) {
        return retroService.getRetro(retroId).orElseThrow();
    }

    @PutMapping("/{retroId}/finished")
    @PreAuthorize("@retroAuthorizationService.isUserAllowedInRetro(authentication, #retroId)")
    public ResponseEntity<Void> updateFinished(@PathVariable("retroId") UUID retroId, @RequestBody UpdateRetroFinishedRequest request) throws RetroNotFoundException {
        retroService.setFinished(retroId, request.finished());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler({InvalidTemplateIdException.class})
    public ResponseEntity<Void> handleInvalidTemplateId() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler({RetroNotFoundException.class})
    public ResponseEntity<Void> handleRetroNotFound() {
        return ResponseEntity.notFound().build();
    }
}
