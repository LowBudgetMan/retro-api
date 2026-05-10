package io.nickreuter.retroapi.team.webhook;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/webhooks")
public class WebhookController {
    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<List<WebhookView>> getWebhooks(@PathVariable UUID teamId) {
        var views = webhookService.getWebhooksForTeam(teamId).stream().map(WebhookView::from).toList();
        return ResponseEntity.ok(views);
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<CreateWebhookResponse> createWebhook(@PathVariable UUID teamId,
                                                                @RequestBody CreateWebhookRequest request,
                                                                @AuthenticationPrincipal Jwt jwt) {
        var created = webhookService.createWebhook(teamId, request.name(), request.url(), request.eventTypes(), jwt.getSubject());
        var entity = created.entity();
        var body = new CreateWebhookResponse(
            entity.getId(), entity.getName(), entity.getUrl(),
            request.eventTypes(), entity.isEnabled(), created.secret()
        );
        return ResponseEntity.created(URI.create("/api/teams/%s/webhooks/%s".formatted(teamId, entity.getId()))).body(body);
    }

    @PutMapping("/{webhookId}")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> updateWebhook(@PathVariable UUID teamId, @PathVariable UUID webhookId,
                                              @RequestBody UpdateWebhookRequest request) {
        webhookService.updateWebhook(webhookId, request.name(), request.url(), request.eventTypes(), request.enabled());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{webhookId}")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> deleteWebhook(@PathVariable UUID teamId, @PathVariable UUID webhookId) {
        webhookService.deleteWebhook(webhookId);
        return ResponseEntity.noContent().build();
    }
}
