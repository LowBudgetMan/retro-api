package io.nickreuter.retroapi.retro.anonymousparticipant;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
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
        return shareTokenRepository.save(new ShareTokenEntity(null, generateSecureToken(), retroId)).toShareToken();
    }

    public boolean isTokenValid(String token) {
        return shareTokenRepository.findByToken(token).isPresent();
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
