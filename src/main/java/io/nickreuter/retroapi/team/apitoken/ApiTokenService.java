package io.nickreuter.retroapi.team.apitoken;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ApiTokenService {
    public static final String TOKEN_PREFIX = "retro_pat_";
    public static final Set<String> VALID_SCOPES = Set.of("read", "write");
    private static final int TOKEN_BYTES = 32;
    private static final int PREFIX_DISPLAY_LENGTH = 14;

    private final ApiTokenRepository repository;
    private final SecureRandom random;

    public ApiTokenService(ApiTokenRepository repository, SecureRandom random) {
        this.repository = repository;
        this.random = random;
    }

    public record CreatedToken(ApiTokenEntity entity, String token) {}

    public CreatedToken createToken(UUID teamId, String name, Set<String> scopes, Instant expiresAt, String createdByUserId) {
        if (scopes.isEmpty() || !VALID_SCOPES.containsAll(scopes)) {
            throw new InvalidScopeException("Scopes must be a non-empty subset of " + VALID_SCOPES);
        }
        var token = generateToken();
        var entity = new ApiTokenEntity(
            null, teamId, name, hash(token), token.substring(0, PREFIX_DISPLAY_LENGTH),
            String.join(",", scopes), null, createdByUserId, expiresAt, null
        );
        return new CreatedToken(repository.save(entity), token);
    }

    public Optional<ApiTokenEntity> findByTokenString(String token) {
        return repository.findByTokenHash(hash(token))
            .filter(entity -> entity.getExpiresAt() == null || entity.getExpiresAt().isAfter(Instant.now()));
    }

    public void touchLastUsed(UUID tokenId) {
        repository.findById(tokenId).ifPresent(entity -> {
            entity.setLastUsedAt(Instant.now());
            repository.save(entity);
        });
    }

    public List<ApiTokenEntity> getTokensForTeam(UUID teamId) {
        return repository.findAllByTeamId(teamId);
    }

    public void deleteToken(UUID tokenId) {
        repository.deleteById(tokenId);
    }

    private String generateToken() {
        var bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
