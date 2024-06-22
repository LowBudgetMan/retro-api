package io.nickreuter.retroapi.retro.thought;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThoughtRepository extends JpaRepository<ThoughtEntity, UUID> {
}
