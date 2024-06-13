package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.thought.ThoughtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Set;
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
    private boolean finished;
    private int templateId;
    @OneToMany(mappedBy = "retroId")
    private Set<ThoughtEntity> thoughts;
    @CreationTimestamp
    private Instant createdAt;

    public RetroEntity(UUID teamId, int templateId) {
        this.teamId = teamId;
        this.templateId = templateId;
    }
}
