package io.nickreuter.retroapi.features;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamSubscriptionRepository extends JpaRepository<TeamSubscriptionEntity, UUID> {
}
