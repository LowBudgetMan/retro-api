package io.nickreuter.retroapi.team.invite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InviteRepository extends JpaRepository<InviteEntity, UUID> {
    Optional<InviteEntity> findByIdAndTeamId(UUID id, UUID teamId);
    List<InviteEntity> findAllByTeamId(UUID teamId);
}
