package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShareTokenService {
    private static final int TOKEN_LENGTH = 64;

    private final ShareTokenRepository shareTokenRepository;
    private final SecureRandom secureRandom;

    public ShareTokenService(ShareTokenRepository shareTokenRepository, SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        this.shareTokenRepository = shareTokenRepository;
    }

    public ShareToken createShareToken(UUID retroId) {
        return shareTokenRepository.save(new ShareTokenEntity(generateSecureToken(), retroId)).toShareToken();
    }

    public List<ShareToken> getShareTokensForRetro(UUID retroId) {
        return shareTokenRepository.findAllByRetroId(retroId).stream().map(ShareTokenEntity::toShareToken).toList();
    }

    public Optional<ShareToken> getShareToken(String token) {
        return shareTokenRepository.findByToken(token).map(ShareTokenEntity::toShareToken);
    }

    public boolean isTokenValid(String token) {
        return shareTokenRepository.findByToken(token).isPresent();
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public void clearShareTokensForRetro(UUID retroId) {
        shareTokenRepository.deleteAllByRetroId(retroId);
    }
}
