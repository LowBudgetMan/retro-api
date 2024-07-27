package io.nickreuter.retroapi.team.actionitem;

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

@Entity(name = "action_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActionItemEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private String action;
    private boolean completed;
    private UUID teamId;
    private String assignee;
    @CreationTimestamp
    private Instant createdAt;

    public static ActionItemEntity from(String action, String assignee, UUID teamId) {
        return new ActionItemEntity(null, action, false, teamId, assignee, null);
    }
}
