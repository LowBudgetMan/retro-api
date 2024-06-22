package io.nickreuter.retroapi.retro.thought;

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

@Entity(name="thought")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ThoughtEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private String message;
    private int votes;
    private boolean completed;
    private String category;
    private UUID retroId;
    @CreationTimestamp
    private Instant createdAt;

    static ThoughtEntity from(String message, String category, UUID retroId) {
        return new ThoughtEntity(null, message, 0, false, category, retroId, null);
    }
}
