package io.nickreuter.retroapi.team.apitoken;

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

@Entity(name = "api_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiTokenEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID teamId;
    private String name;
    private String tokenHash;
    private String tokenPrefix;
    private String scopes;
    @CreationTimestamp
    private Instant createdAt;
    private String createdByUserId;
    private Instant expiresAt;
    private Instant lastUsedAt;
}
