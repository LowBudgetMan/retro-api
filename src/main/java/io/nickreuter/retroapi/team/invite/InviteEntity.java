package io.nickreuter.retroapi.team.invite;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "invite")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class InviteEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID teamId;
    @CreationTimestamp
    private Instant createdAt;
}
