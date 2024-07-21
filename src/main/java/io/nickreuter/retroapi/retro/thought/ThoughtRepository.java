package io.nickreuter.retroapi.retro.thought;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ThoughtRepository extends JpaRepository<ThoughtEntity, UUID> {
    List<ThoughtEntity> findByRetroIdOrderByCreatedAtDesc(UUID retroId);
    @Transactional
    @Modifying
    @Query("UPDATE thought thought set thought.votes = thought.votes + 1 where thought.id = :thoughtId")
    void incrementVotes(@Param("thoughtId") UUID thoughtId);
}
