package io.nickreuter.retroapi.share;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShareTokenRepository extends JpaRepository<ShareTokenEntity, UUID> {
    
    Optional<ShareTokenEntity> findByToken(String token);
    
    @Query("SELECT st FROM ShareTokenEntity st WHERE st.retroId = :retroId AND st.active = true")
    java.util.List<ShareTokenEntity> findActiveByRetroId(@Param("retroId") UUID retroId);
    
    void deleteByRetroId(UUID retroId);
}
