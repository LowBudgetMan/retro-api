package io.nickreuter.retroapi.share;

import io.nickreuter.retroapi.retro.RetroService;
import io.nickreuter.retroapi.team.usermapping.UserMappingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ShareTokenService {
    
    private static final int TOKEN_LENGTH = 32;
    private static final Duration DEFAULT_EXPIRATION = Duration.ofDays(30);
    private static final Integer DEFAULT_MAX_USES = null; // Unlimited by default
    
    private final ShareTokenRepository shareTokenRepository;
    private final RetroService retroService;
    private final UserMappingService userMappingService;
    private final SecureRandom secureRandom;
    
    public ShareTokenService(ShareTokenRepository shareTokenRepository, 
                           RetroService retroService, 
                           UserMappingService userMappingService) {
        this.shareTokenRepository = shareTokenRepository;
        this.retroService = retroService;
        this.userMappingService = userMappingService;
        this.secureRandom = new SecureRandom();
    }
    
    public ShareToken createShareToken(UUID retroId, UUID createdBy, Duration expiration, Integer maxUses) {
        // Validate that the user has access to this retro
        if (!retroService.getRetro(retroId).isPresent()) {
            throw new IllegalArgumentException("Retro not found");
        }
        
        // Generate secure token
        String token = generateSecureToken();
        
        // Set expiration
        Instant expiresAt = expiration != null ? 
            Instant.now().plus(expiration) : 
            Instant.now().plus(DEFAULT_EXPIRATION);
        
        // Default max uses if not specified
        Integer finalMaxUses = maxUses != null ? maxUses : DEFAULT_MAX_USES;
        
        ShareTokenEntity entity = new ShareTokenEntity(
            UUID.randomUUID(),
            token,
            retroId,
            createdBy,
            Instant.now(),
            expiresAt,
            finalMaxUses,
            0,
            true
        );
        
        ShareTokenEntity saved = shareTokenRepository.save(entity);
        return ShareToken.fromEntity(saved);
    }
    
    public ShareTokenEntity validateAndUseToken(String token) {
        ShareTokenEntity shareToken = shareTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid share token"));
        
        if (!shareToken.isValid()) {
            throw new IllegalArgumentException("Share token is expired or invalid");
        }
        
        // Increment usage count
        shareToken.incrementUses();
        shareTokenRepository.save(shareToken);
        
        return shareToken;
    }
    
    public ShareTokenEntity validateTokenWithoutUsage(String token) {
        ShareTokenEntity shareToken = shareTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid share token"));
        
        if (!shareToken.isValid()) {
            throw new IllegalArgumentException("Share token is expired or invalid");
        }
        
        return shareToken;
    }
    
    public List<ShareToken> getActiveShareTokensForRetro(UUID retroId) {
        return shareTokenRepository.findActiveByRetroId(retroId)
            .stream()
            .map(ShareToken::fromEntity)
            .toList();
    }
    
    public void deactivateToken(UUID tokenId) {
        ShareTokenEntity token = shareTokenRepository.findById(tokenId)
            .orElseThrow(() -> new IllegalArgumentException("Share token not found"));
        
        token.setActive(false);
        shareTokenRepository.save(token);
    }
    
    public void deactivateAllTokensForRetro(UUID retroId) {
        List<ShareTokenEntity> activeTokens = shareTokenRepository.findActiveByRetroId(retroId);
        for (ShareTokenEntity token : activeTokens) {
            token.setActive(false);
        }
        shareTokenRepository.saveAll(activeTokens);
    }
    
    public void deleteTokensForRetro(UUID retroId) {
        shareTokenRepository.deleteByRetroId(retroId);
    }
    
    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.urlEncoding().withoutPadding().encodeToString(randomBytes);
    }
}
