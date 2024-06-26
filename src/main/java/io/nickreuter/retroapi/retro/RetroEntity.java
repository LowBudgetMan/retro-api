package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.thought.ThoughtEntity;
import jakarta.persistence.*;
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
    private String templateId;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "retroId")
    private Set<ThoughtEntity> thoughts;
    @CreationTimestamp
    private Instant createdAt;

    public RetroEntity(UUID teamId, String templateId) {
        this.teamId = teamId;
        this.templateId = templateId;
    }
}
