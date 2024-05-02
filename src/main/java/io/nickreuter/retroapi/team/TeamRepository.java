package io.nickreuter.retroapi.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
    boolean existsByName(String name);
    List<TeamEntity> findAllByIdInOrderByNameAsc(Set<UUID> ids);
}
