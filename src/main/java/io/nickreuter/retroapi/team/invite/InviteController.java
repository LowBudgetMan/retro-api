package io.nickreuter.retroapi.team.invite;

import io.nickreuter.retroapi.team.exception.TeamNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/invites")
public class InviteController {

    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> createInvite(@PathVariable("teamId") UUID teamId) throws TeamNotFoundException {
        var invite = inviteService.createInvite(teamId);
        return ResponseEntity.created(URI.create("/api/teams/%s/invites/%s".formatted(teamId, invite.getId()))).build();
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<Void> handleTeamNotFoundException() {
        return ResponseEntity.notFound().build();
    }
}
