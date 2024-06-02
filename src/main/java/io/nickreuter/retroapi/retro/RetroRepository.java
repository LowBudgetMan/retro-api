package io.nickreuter.retroapi.retro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RetroRepository extends JpaRepository<RetroEntity, UUID> {
}
