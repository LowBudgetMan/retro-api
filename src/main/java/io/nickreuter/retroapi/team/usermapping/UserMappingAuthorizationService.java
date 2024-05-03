package io.nickreuter.retroapi.team.usermapping;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserMappingAuthorizationService {
    private final UserMappingRepository userMappingRepository;

    public UserMappingAuthorizationService(UserMappingRepository userMappingRepository) {
        this.userMappingRepository = userMappingRepository;
    }

    public boolean isUserMemberOfTeam(Authentication authentication, UUID teamId) {
        return userMappingRepository.findByTeamIdAndUserId(teamId, authentication.getName()).isPresent();
    }
}
