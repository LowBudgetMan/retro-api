package io.nickreuter.retroapi.team.apitoken;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams/{teamId}/api-tokens")
public class ApiTokenController {
    private final ApiTokenService apiTokenService;

    public ApiTokenController(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }

    @GetMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<List<ApiTokenView>> getTokens(@PathVariable UUID teamId) {
        var views = apiTokenService.getTokensForTeam(teamId).stream().map(ApiTokenView::from).toList();
        return ResponseEntity.ok(views);
    }

    @PostMapping
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<CreateApiTokenResponse> createToken(@PathVariable UUID teamId,
                                                              @RequestBody CreateApiTokenRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        var created = apiTokenService.createToken(teamId, request.name(), request.scopes(), request.expiresAt(), jwt.getSubject());
        var entity = created.entity();
        var body = new CreateApiTokenResponse(
            entity.getId(), entity.getName(),
            request.scopes(), entity.getExpiresAt(),
            entity.getTokenPrefix(), created.token()
        );
        return ResponseEntity.created(URI.create("/api/teams/%s/api-tokens/%s".formatted(teamId, entity.getId()))).body(body);
    }

    @DeleteMapping("/{tokenId}")
    @PreAuthorize("@userMappingAuthorizationService.isUserMemberOfTeam(authentication, #teamId)")
    public ResponseEntity<Void> deleteToken(@PathVariable UUID teamId, @PathVariable UUID tokenId) {
        apiTokenService.deleteToken(tokenId);
        return ResponseEntity.noContent().build();
    }
}
