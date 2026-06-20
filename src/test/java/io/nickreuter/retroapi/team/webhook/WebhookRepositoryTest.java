package io.nickreuter.retroapi.team.webhook;

import io.nickreuter.retroapi.team.TeamEntity;
import io.nickreuter.retroapi.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WebhookRepositoryTest {
    @Autowired
    private WebhookRepository webhookRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Test
    void findAllByTeamId_ReturnsWebhooksForTeam() {
        var team = teamRepository.save(new TeamEntity(null, "Team A", Instant.now()));
        var otherTeam = teamRepository.save(new TeamEntity(null, "Team B", Instant.now()));
        var webhook1 = new WebhookEntity(null, team.getId(), "Hook 1", "https://example.com/hook1", "secret1", "action_item.created", null);
        var webhook2 = new WebhookEntity(null, team.getId(), "Hook 2", "https://example.com/hook2", "secret2", "retro.finished", null);
        var otherWebhook = new WebhookEntity(null, otherTeam.getId(), "Other", "https://example.com/other", "secret3", "action_item.created", null);

        webhookRepository.save(webhook1);
        webhookRepository.save(webhook2);
        webhookRepository.save(otherWebhook);

        var result = webhookRepository.findAllByTeamId(team.getId());
        assertThat(result).hasSize(2);
        assertThat(result).extracting(WebhookEntity::getName).containsExactlyInAnyOrder("Hook 1", "Hook 2");
    }
}
