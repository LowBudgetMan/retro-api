package io.nickreuter.retroapi.configuration.jwt;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Universal JWT Decoder that handles tokens from any OAuth provider
 * by dynamically creating decoders based on the issuer extracted from each token.
 * 
 * This decoder:
 * 1. Extracts the issuer from each incoming JWT token
 * 2. Creates a specialized decoder for that issuer using AllTypeJwtDecoderFactory
 * 3. Caches decoders by issuer for performance
 * 4. Handles all JWT types (JWT, at+jwt, id+token, etc.)
 */
public class UniversalJwtDecoder implements JwtDecoder {

    private final AllTypeJwtDecoderFactory decoderFactory;
    private final Map<String, JwtDecoder> decoderCache = new ConcurrentHashMap<>();

    public UniversalJwtDecoder(AllTypeJwtDecoderFactory decoderFactory) {
        this.decoderFactory = decoderFactory;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Parse the JWT to extract the issuer
            JWT jwt = JWTParser.parse(token);
            
            // Extract issuer from the token
            String issuer = extractIssuer(jwt);
            if (issuer == null) {
                throw new JwtException("JWT does not contain an issuer claim");
            }

            // Get or create the appropriate decoder for this issuer
            JwtDecoder decoder = decoderCache.computeIfAbsent(issuer, decoderFactory::createDecoder);
            
            // Use the issuer-specific decoder to decode the token
            return decoder.decode(token);
            
        } catch (ParseException e) {
            throw new JwtException("Failed to parse JWT", e);
        } catch (Exception e) {
            throw new JwtException("Failed to decode JWT", e);
        }
    }

    /**
     * Extracts the issuer from a JWT token.
     * 
     * @param jwt The parsed JWT token
     * @return The issuer URI, or null if not found
     */
    private String extractIssuer(JWT jwt) {
        try {
            return jwt.getJWTClaimsSet().getIssuer();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clears the decoder cache. Useful for testing or when configuration changes.
     */
    public void clearCache() {
        decoderCache.clear();
    }
}
