package io.nickreuter.retroapi.team.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamSubscriptionRepository extends JpaRepository<TeamSubscriptionEntity, UUID> {
    Optional<TeamSubscriptionEntity> findByTeamId(UUID teamId);
}
