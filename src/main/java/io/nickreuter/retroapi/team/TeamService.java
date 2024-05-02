package io.nickreuter.retroapi.team;

import io.nickreuter.retroapi.team.exception.TeamAlreadyExistsException;
import io.nickreuter.retroapi.team.usermapping.UserMappingEntity;
import io.nickreuter.retroapi.team.usermapping.UserMappingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserMappingService userMappingService;

    public TeamService(TeamRepository teamRepository, UserMappingService userMappingService) {
        this.teamRepository = teamRepository;
        this.userMappingService = userMappingService;
    }

    public TeamEntity createTeam(String name, String userId) throws TeamAlreadyExistsException {
        if (teamRepository.existsByName(name)) throw new TeamAlreadyExistsException();
        var team = teamRepository.save(new TeamEntity(name));
        userMappingService.addUserToTeam(userId, team.getId());
        return team;
    }

    public List<TeamEntity> getTeamsForUser(String userId) {
        var teamIds = userMappingService.getTeamsForUser(userId).stream()
                .map(UserMappingEntity::getTeamId)
                .collect(Collectors.toSet());
        return teamRepository.findAllByIdInOrderByNameAsc(teamIds);
    }
}
