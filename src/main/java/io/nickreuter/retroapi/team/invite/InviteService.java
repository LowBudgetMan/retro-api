package io.nickreuter.retroapi.team.invite;

import io.nickreuter.retroapi.team.TeamService;
import io.nickreuter.retroapi.team.exception.TeamNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InviteService {
    private final TeamService teamService;
    private final InviteRepository inviteRepository;

    public InviteService(TeamService teamService, InviteRepository inviteRepository) {
        this.teamService = teamService;
        this.inviteRepository = inviteRepository;
    }

    public InviteEntity createInvite(UUID teamId) throws TeamNotFoundException {
        if(teamService.getTeam(teamId).isPresent()) {
            return inviteRepository.save(new InviteEntity(null, teamId, null));
        } else {
            throw new TeamNotFoundException();
        }
    }
}
