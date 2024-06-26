package io.nickreuter.retroapi.team.usermapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserMappingRepository extends JpaRepository<UserMappingEntity, UUID> {
    Set<UserMappingEntity> findAllByUserId(String userId);
    Optional<UserMappingEntity> findByTeamIdAndUserId(UUID teamId, String userId);
    void deleteAllByTeamIdAndUserId(UUID teamId, String userId);
}
