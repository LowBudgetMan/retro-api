package io.nickreuter.retroapi.team.usermapping;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "team_user_mapping")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserMappingEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID teamId;
    private String userId;
    @CreationTimestamp
    private Instant createdAt;
}
