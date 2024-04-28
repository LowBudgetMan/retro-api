package io.nickreuter.retroapi.team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity(name="team")
@NoArgsConstructor
@AllArgsConstructor
public class TeamEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(unique = true)
    private String name;
    @CreationTimestamp
    private Instant createdAt;

    public TeamEntity(String name) {
        this.name = name;
    }
}
