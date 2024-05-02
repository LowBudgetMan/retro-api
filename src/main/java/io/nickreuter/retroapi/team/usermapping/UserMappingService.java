package io.nickreuter.retroapi.team.usermapping;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class UserMappingService {
    private final UserMappingRepository userMappingRepository;

    public UserMappingService(UserMappingRepository userMappingRepository) {
        this.userMappingRepository = userMappingRepository;
    }

    public void addUserToTeam(String userId, UUID teamId) {
        userMappingRepository.save(new UserMappingEntity(null, teamId, userId, null));
    }

    public Set<UserMappingEntity> getTeamsForUser(String userId) {
        return userMappingRepository.findAllByUserId(userId);
    }
}
