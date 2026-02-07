package io.nickreuter.retroapi.share;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "share_tokens")
public class ShareTokenEntity {
    @Id
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private UUID retroId;
    
    @Column(nullable = false)
    private UUID createdBy;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column
    private Instant expiresAt;
    
    @Column
    private Integer maxUses;
    
    @Column
    private Integer uses;
    
    @Column(nullable = false)
    private boolean active;
    
    protected ShareTokenEntity() {}
    
    public ShareTokenEntity(UUID id, String token, UUID retroId, UUID createdBy, Instant createdAt, 
                          Instant expiresAt, Integer maxUses, Integer uses, boolean active) {
        this.id = id;
        this.token = token;
        this.retroId = retroId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.uses = uses;
        this.active = active;
    }
    
    public UUID getId() { return id; }
    public String getToken() { return token; }
    public UUID getRetroId() { return retroId; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Integer getMaxUses() { return maxUses; }
    public Integer getUses() { return uses; }
    public boolean isActive() { return active; }
    
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }
    
    public boolean isUsageLimitReached() {
        return maxUses != null && uses >= maxUses;
    }
    
    public void incrementUses() {
        if (this.uses == null) {
            this.uses = 1;
        } else {
            this.uses++;
        }
    }
    
    public boolean isValid() {
        return active && !isExpired() && !isUsageLimitReached();
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
