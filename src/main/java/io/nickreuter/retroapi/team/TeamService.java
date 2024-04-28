package io.nickreuter.retroapi.team;

import io.nickreuter.retroapi.team.exception.TeamAlreadyExistsException;
import io.nickreuter.retroapi.team.usermapping.UserMappingService;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserMappingService userMappingService;

    public TeamService(TeamRepository teamRepository, UserMappingService userMappingService) {
        this.teamRepository = teamRepository;
        this.userMappingService = userMappingService;
    }

    public TeamEntity createTeam(String name, String userId) throws TeamAlreadyExistsException {
        if(teamRepository.existsByName(name)) throw new TeamAlreadyExistsException();
        var team = teamRepository.save(new TeamEntity(name));
        userMappingService.addUserToTeam(userId, team.getId());
        return team;
    }
}
