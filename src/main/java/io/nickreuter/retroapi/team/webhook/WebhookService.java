package io.nickreuter.retroapi.team.webhook;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class WebhookService {
    public static final Set<String> VALID_EVENT_TYPES = Set.of(
        "action_item.created", "action_item.updated", "action_item.deleted", "retro.finished"
    );
    private static final int SECRET_BYTES = 32;

    private final WebhookRepository repository;
    private final SecureRandom random;

    public WebhookService(WebhookRepository repository, SecureRandom random) {
        this.repository = repository;
        this.random = random;
    }

    public record CreatedWebhook(WebhookEntity entity, String secret) {}

    public CreatedWebhook createWebhook(UUID teamId, String name, String url, Set<String> eventTypes, String createdByUserId) {
        if (!url.startsWith("https://")) {
            throw new IllegalArgumentException("Webhook URL must use HTTPS");
        }
        if (eventTypes.isEmpty() || !VALID_EVENT_TYPES.containsAll(eventTypes)) {
            throw new InvalidEventTypeException("Event types must be a non-empty subset of " + VALID_EVENT_TYPES);
        }
        var secret = generateSecret();
        var entity = new WebhookEntity(
            null, teamId, name, url, secret,
            String.join(",", eventTypes),
            true, 0, null, null, null, null, createdByUserId
        );
        return new CreatedWebhook(repository.save(entity), secret);
    }

    public List<WebhookEntity> getWebhooksForTeam(UUID teamId) {
        return repository.findAllByTeamId(teamId);
    }

    public List<WebhookEntity> getEnabledWebhooksForTeam(UUID teamId) {
        return repository.findAllByTeamIdAndEnabledTrue(teamId);
    }

    public void updateWebhook(UUID webhookId, String name, String url, Set<String> eventTypes, boolean enabled) {
        var entity = repository.findById(webhookId).orElseThrow();
        if (url != null && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Webhook URL must use HTTPS");
        }
        if (eventTypes != null && !VALID_EVENT_TYPES.containsAll(eventTypes)) {
            throw new InvalidEventTypeException("Event types must be a subset of " + VALID_EVENT_TYPES);
        }
        if (name != null) entity.setName(name);
        if (url != null) entity.setUrl(url);
        if (eventTypes != null) entity.setEventTypes(String.join(",", eventTypes));
        entity.setEnabled(enabled);
        repository.save(entity);
    }

    public void deleteWebhook(UUID webhookId) {
        repository.deleteById(webhookId);
    }

    public void recordSuccess(UUID webhookId) {
        repository.findById(webhookId).ifPresent(entity -> {
            entity.setConsecutiveFailures(0);
            entity.setLastDeliveryAt(Instant.now());
            repository.save(entity);
        });
    }

    public void recordFailure(UUID webhookId, String reason) {
        repository.findById(webhookId).ifPresent(entity -> {
            entity.setConsecutiveFailures(entity.getConsecutiveFailures() + 1);
            entity.setLastDeliveryAt(Instant.now());
            entity.setLastFailureAt(Instant.now());
            entity.setLastFailureReason(reason);
            if (entity.getConsecutiveFailures() >= 5) {
                entity.setEnabled(false);
            }
            repository.save(entity);
        });
    }

    private String generateSecret() {
        var bytes = new byte[SECRET_BYTES];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
