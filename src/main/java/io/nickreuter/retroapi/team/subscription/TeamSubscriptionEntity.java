package io.nickreuter.retroapi.team.subscription;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity(name = "team_subscription")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamSubscriptionEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID teamId;
    @Enumerated(EnumType.STRING)
    private Plan plan;
    @Enumerated(EnumType.STRING)
    private PlanStatus planStatus;
    @CreationTimestamp
    private Date createdAt;
    @UpdateTimestamp
    private Date modifiedAt;
}
