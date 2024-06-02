package io.nickreuter.retroapi.retro;

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

@Entity(name="retro")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RetroEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID teamId;
    @CreationTimestamp
    private Instant createdAt;

    public RetroEntity(UUID teamId) {
        this.teamId = teamId;
    }
}