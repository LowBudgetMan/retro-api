package io.nickreuter.retroapi.team.actionitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItemEntity, UUID> {
}
