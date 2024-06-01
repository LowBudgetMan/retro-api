package io.nickreuter.retroapi.team;

import io.nickreuter.retroapi.team.exception.BadInviteException;
import io.nickreuter.retroapi.team.exception.TeamAlreadyExistsException;
import io.nickreuter.retroapi.team.invite.InviteEntity;
import io.nickreuter.retroapi.team.invite.InviteService;
import io.nickreuter.retroapi.team.usermapping.UserMappingEntity;
import io.nickreuter.retroapi.team.usermapping.UserMappingService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserMappingService userMappingService;
    private final InviteService inviteService;

    public TeamService(TeamRepository teamRepository, UserMappingService userMappingService, InviteService inviteService) {
        this.teamRepository = teamRepository;
        this.userMappingService = userMappingService;
        this.inviteService = inviteService;
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

    public Optional<TeamEntity> getTeam(UUID teamId) {
        return teamRepository.findById(teamId);
    }

    public void addUser(UUID teamId, String userId, UUID inviteId) throws BadInviteException {
        var maybeInvite = inviteService.getInviteForTeam(teamId, inviteId);
        if (isValidInvite(maybeInvite)) userMappingService.addUserToTeam(userId, teamId);
        else throw new BadInviteException();
    }

    private boolean isValidInvite(Optional<InviteEntity> maybeInvite) {
        return maybeInvite.map(inviteEntity -> inviteEntity.getCreatedAt().plus(3, ChronoUnit.HOURS)
                .isAfter(Instant.now()))
                .orElse(false);
    }

    public void removeUser(UUID teamId, String userId) {
        userMappingService.removeUserFromTeam(teamId, userId);
    }
}
