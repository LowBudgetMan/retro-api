package io.nickreuter.retroapi.team.invite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InviteRepository extends JpaRepository<InviteEntity, UUID> {
}
