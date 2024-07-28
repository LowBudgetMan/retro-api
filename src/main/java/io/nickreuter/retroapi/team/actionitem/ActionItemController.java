package io.nickreuter.retroapi.team.actionitem;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/action-items")
public class ActionItemController {
    private final ActionItemService actionItemService;

    public ActionItemController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> createActionItem(@PathVariable("teamId") String teamId, @RequestBody CreateActionItemRequest request) {
        var actionItem = actionItemService.createActionItem(request.action(), request.assignee(), request.teamId());
        return ResponseEntity.created(URI.create("/api/teams/%s/action-items/%s".formatted(teamId, actionItem.getId()))).build();
    }

    @PutMapping("/{actionItemId}/action")
    @PreAuthorize("@actionItemAuthorizationService.canUserModifyActionItem(authentication, #actionItemId)")
    public ResponseEntity<Void> setAction(@PathVariable UUID teamId, @PathVariable UUID actionItemId, @RequestBody UpdateActionItemActionRequest request) {
        actionItemService.setAction(actionItemId, request.action());
        return ResponseEntity.noContent().build();
    }
}
