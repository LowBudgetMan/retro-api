package io.nickreuter.retroapi.team.usermapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserMappingRepository extends JpaRepository<UserMappingEntity, UUID> {
}
