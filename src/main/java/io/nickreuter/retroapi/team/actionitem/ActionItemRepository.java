package io.nickreuter.retroapi.team.actionitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItemEntity, UUID> {
    List<ActionItemEntity> findAllByTeamId(UUID teamId);
}
