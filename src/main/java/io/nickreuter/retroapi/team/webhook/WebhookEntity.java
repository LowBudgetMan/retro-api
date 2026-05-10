package io.nickreuter.retroapi.team.webhook;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "webhook")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID teamId;
    private String name;
    private String url;
    private String secret;
    private String eventTypes;
    private boolean enabled;
    private int consecutiveFailures;
    private Instant lastDeliveryAt;
    private Instant lastFailureAt;
    private String lastFailureReason;
    @CreationTimestamp
    private Instant createdAt;
    private String createdByUserId;
}
