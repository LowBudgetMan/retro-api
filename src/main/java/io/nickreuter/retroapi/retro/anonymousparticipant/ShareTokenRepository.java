package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShareTokenRepository extends JpaRepository<ShareTokenEntity, UUID> {
    Optional<ShareTokenEntity> findByToken(String shareToken);
    List<ShareTokenEntity> findAllByRetroId(UUID retroId);

    @Transactional
    void deleteAllByRetroId(UUID retroId);
}
