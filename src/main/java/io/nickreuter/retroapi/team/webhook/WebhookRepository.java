package io.nickreuter.retroapi.team.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookRepository extends JpaRepository<WebhookEntity, UUID> {
    List<WebhookEntity> findAllByTeamId(UUID teamId);
    List<WebhookEntity> findAllByTeamIdAndEnabledTrue(UUID teamId);
}
