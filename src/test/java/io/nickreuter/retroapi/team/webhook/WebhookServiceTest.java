package io.nickreuter.retroapi.team.webhook;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebhookServiceTest {
    private final WebhookRepository repository = mock(WebhookRepository.class);
    private final SecureRandom random = new SecureRandom();
    private final WebhookService subject = new WebhookService(repository, random);

    @Test
    void createWebhook_WithValidInput_CreatesWebhookWithGeneratedSecret() {
        var teamId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> {
            var entity = (WebhookEntity) inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var result = subject.createWebhook(teamId, "Slack", "https://hooks.slack.com/test", Set.of("action_item.created"), "user1");

        assertThat(result.entity().getName()).isEqualTo("Slack");
        assertThat(result.entity().getUrl()).isEqualTo("https://hooks.slack.com/test");
        assertThat(result.entity().getEventTypes()).isEqualTo("action_item.created");
        assertThat(result.entity().isEnabled()).isTrue();
        assertThat(result.entity().getConsecutiveFailures()).isZero();
        assertThat(result.secret()).hasSize(64);
    }

    @Test
    void createWebhook_WithHttpUrl_ThrowsException() {
        var teamId = UUID.randomUUID();
        assertThatThrownBy(() -> subject.createWebhook(teamId, "Bad", "http://example.com", Set.of("action_item.created"), "user1"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createWebhook_WithInvalidEventType_ThrowsException() {
        var teamId = UUID.randomUUID();
        assertThatThrownBy(() -> subject.createWebhook(teamId, "Bad", "https://example.com", Set.of("invalid.event"), "user1"))
            .isInstanceOf(InvalidEventTypeException.class);
    }

    @Test
    void createWebhook_WithMultipleEventTypes_StoresCommaSeparated() {
        var teamId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = subject.createWebhook(teamId, "Hook", "https://example.com", Set.of("action_item.created", "retro.finished"), "user1");

        var storedTypes = result.entity().getEventTypes();
        assertThat(storedTypes).contains("action_item.created");
        assertThat(storedTypes).contains("retro.finished");
    }

    @Test
    void getWebhooksForTeam_DelegatesToRepository() {
        var teamId = UUID.randomUUID();
        subject.getWebhooksForTeam(teamId);
        verify(repository).findAllByTeamId(teamId);
    }

    @Test
    void deleteWebhook_DelegatesToRepository() {
        var webhookId = UUID.randomUUID();
        subject.deleteWebhook(webhookId);
        verify(repository).deleteById(webhookId);
    }

    @Test
    void updateWebhook_UpdatesFields() {
        var webhookId = UUID.randomUUID();
        var existing = new WebhookEntity(webhookId, UUID.randomUUID(), "Old", "https://old.com", "secret", "action_item.created", true, 0, null, null, null, null, "user1");
        when(repository.findById(webhookId)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        subject.updateWebhook(webhookId, "New", "https://new.com", Set.of("retro.finished"), true);

        verify(repository).save(argThat(entity ->
            entity.getName().equals("New") &&
            entity.getUrl().equals("https://new.com") &&
            entity.getEventTypes().equals("retro.finished") &&
            entity.isEnabled()
        ));
    }
}
