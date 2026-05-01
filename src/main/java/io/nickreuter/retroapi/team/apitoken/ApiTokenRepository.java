package io.nickreuter.retroapi.team.apitoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiTokenRepository extends JpaRepository<ApiTokenEntity, UUID> {
    Optional<ApiTokenEntity> findByTokenHash(String tokenHash);
    List<ApiTokenEntity> findAllByTeamId(UUID teamId);
}
