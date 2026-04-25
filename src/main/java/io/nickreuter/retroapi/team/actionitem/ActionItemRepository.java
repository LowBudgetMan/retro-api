package io.nickreuter.retroapi.team.actionitem;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItemEntity, UUID> {
    List<ActionItemEntity> findAllByTeamIdAndArchived(UUID teamId, boolean isArchived);

    @Modifying
    @Transactional
    @Query("UPDATE action_item actionItem SET actionItem.archived = true WHERE actionItem.completed = true AND actionItem.teamId = :teamId")
    void archiveCompletedActionItemsForTeam(@Param("teamId") UUID teamId);
}
