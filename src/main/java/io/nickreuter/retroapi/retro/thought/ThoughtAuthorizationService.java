package io.nickreuter.retroapi.retro.thought;

import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ThoughtAuthorizationService {
    private final ThoughtService thoughtService;
    private final RetroAuthorizationService retroAuthorizationService;

    public ThoughtAuthorizationService(ThoughtService thoughtService, RetroAuthorizationService retroAuthorizationService) {
        this.thoughtService = thoughtService;
        this.retroAuthorizationService = retroAuthorizationService;
    }

    public boolean canUserModifyThought(Authentication authentication, UUID thoughtId) {
        var thought = thoughtService.getThought(thoughtId);
        return thought.isPresent() && retroAuthorizationService.isUserAllowedInRetro(authentication, thought.get().getRetroId());
    }
}
