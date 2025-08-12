package io.nickreuter.retroapi.team.actionitem;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/action-items")
public class ActionItemController {
    private final ActionItemService actionItemService;

    public ActionItemController(ActionItemService actionItemService) {
        this.actionItemService = actionItemService;
    }

    @GetMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<List<ActionItemEntity>> getActionItems(@PathVariable("teamId") UUID teamId) {
        return ResponseEntity.ok(actionItemService.getActionItemsForTeam(teamId));
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> createActionItem(@PathVariable("teamId") UUID teamId, @RequestBody CreateActionItemRequest request) {
        var actionItem = actionItemService.createActionItem(request.action(), request.assignee(), teamId);
        return ResponseEntity.created(URI.create("/api/teams/%s/action-items/%s".formatted(teamId, actionItem.getId()))).build();
    }

    @PutMapping("/{actionItemId}/action")
    @PreAuthorize("@actionItemAuthorizationService.canUserModifyActionItem(authentication, #actionItemId)")
    public ResponseEntity<Void> setAction(@PathVariable UUID teamId, @PathVariable UUID actionItemId, @RequestBody UpdateActionItemActionRequest request) {
        actionItemService.setAction(actionItemId, request.action());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{actionItemId}/assignee")
    @PreAuthorize("@actionItemAuthorizationService.canUserModifyActionItem(authentication, #actionItemId)")
    public ResponseEntity<Void> setAssignee(@PathVariable UUID teamId, @PathVariable UUID actionItemId, @RequestBody UpdateActionItemAssigneeRequest request) {
        actionItemService.setAssignee(actionItemId, request.assignee());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{actionItemId}/completed")
    @PreAuthorize("@actionItemAuthorizationService.canUserModifyActionItem(authentication, #actionItemId)")
    public ResponseEntity<Void> setCompleted(@PathVariable UUID teamId, @PathVariable UUID actionItemId, @RequestBody UpdateActionItemCompletedRequest request) {
        actionItemService.setCompleted(actionItemId, request.completed());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{actionItemId}")
    @PreAuthorize("@actionItemAuthorizationService.canUserModifyActionItem(authentication, #actionItemId)")
    public ResponseEntity<Void> deleteActionItem(@PathVariable UUID teamId, @PathVariable UUID actionItemId) {
        actionItemService.deleteActionItem(actionItemId);
        return ResponseEntity.noContent().build();
    }
}
