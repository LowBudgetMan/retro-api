package io.nickreuter.retroapi.team.invite;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;

    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public InviteEntity createInvite(UUID teamId) {
        return inviteRepository.save(new InviteEntity(null, teamId, null));
    }

    public Optional<InviteEntity> getInviteForTeam(UUID teamId, UUID inviteId) {
        return inviteRepository.findByIdAndTeamId(inviteId, teamId);
    }

    public void deleteInvite(UUID inviteId) {
        inviteRepository.deleteById(inviteId);
    }
}
