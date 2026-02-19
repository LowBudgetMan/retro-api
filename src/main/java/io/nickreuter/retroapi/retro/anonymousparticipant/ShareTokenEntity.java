package io.nickreuter.retroapi.retro.anonymousparticipant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Table(name = "share_token")
public class ShareTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String token;
    @Column(unique = true)
    private UUID retroId;

    public ShareToken toShareToken() {
        return new ShareToken(id, token, retroId);
    }
}
