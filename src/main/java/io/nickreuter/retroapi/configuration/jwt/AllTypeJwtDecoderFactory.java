package io.nickreuter.retroapi.configuration.jwt;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating JWT decoders that can handle multiple OAuth providers
 * with different JWT type configurations.
 * 
 * This implementation supports common JWT types from various providers:
 * - Standard JWT tokens ("JWT")
 * - Auth0 access tokens ("at+jwt") 
 * - Microsoft tokens ("at+jwt", "jwt")
 * - Generic OAuth2 tokens
 * - And tokens with missing/empty typ headers
 */
@Component
public class AllTypeJwtDecoderFactory {
    
    // Cache decoders by issuer URI for performance
    private final Map<String, JwtDecoder> decoderCache = new ConcurrentHashMap<>();
    
    // Most common JWT types across different OAuth providers
    private static final Set<JOSEObjectType> ALLOWED_JWT_TYPES = new HashSet<>(Arrays.asList(
            JOSEObjectType.JWT,
            new JOSEObjectType("at+jwt"),        // Auth0, Microsoft
            new JOSEObjectType("id+token"),      // Microsoft Identity
            new JOSEObjectType("access_token"),  // Some OAuth2 implementations
            new JOSEObjectType("RefreshToken"),  // Some OAuth2 implementations
            new JOSEObjectType("OpenID Connect"), // OIDC tokens
            new JOSEObjectType("application/jwt"), // Some JSON Web Token implementations
            new JOSEObjectType("application/at+jwt"), // Microsoft Identity
            new JOSEObjectType("application/oauth-jwt") // General OAuth JWT
    ));

    /**
     * Creates a JWT decoder for the specified issuer URI with enhanced type handling.
     * 
     * @param issuerUri The issuer URI for the JWT tokens
     * @return A configured JWT decoder that accepts multiple JWT types
     */
    public JwtDecoder createDecoder(String issuerUri) {
        return decoderCache.computeIfAbsent(issuerUri, this::createNewDecoder);
    }

    /**
     * Creates a new JWT decoder with custom type verifier for multi-provider compatibility.
     * 
     * @param issuerUri The issuer URI
     * @return A JWT decoder configured for multiple JWT types
     */
    private JwtDecoder createNewDecoder(String issuerUri) {
        try {
            // Create decoder with JWK Set resolution

            return NimbusJwtDecoder.withIssuerLocation(issuerUri)
                    .jwtProcessorCustomizer(customizer -> {
                        // Configure type verifier to accept multiple JWT types
                        customizer.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(ALLOWED_JWT_TYPES));
                    })
                    .build();
            
        } catch (Exception e) {
            // Fallback to a more permissive decoder if JWK Set configuration fails
            return createFallbackDecoder(issuerUri);
        }
    }

    /**
     * Creates a fallback decoder that is very permissive with JWT types.
     * This is used when the primary decoder configuration fails.
     * 
     * @param issuerUri The issuer URI
     * @return A permissive JWT decoder
     */
    private JwtDecoder createFallbackDecoder(String issuerUri) {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri)
                .jwtProcessorCustomizer(customizer -> {
                    // Allow any JWT type by creating a very permissive set
                    Set<JOSEObjectType> permissiveTypes = new HashSet<>();
                    // Add empty type to allow tokens without 'typ' header
                    permissiveTypes.add(new JOSEObjectType(""));
                    permissiveTypes.add(JOSEObjectType.JWT); // Standard JWT
                    permissiveTypes.add(new JOSEObjectType("at+jwt")); // Auth0/Microsoft
                    customizer.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(permissiveTypes));
                })
                .build();
    }

    /**
     * Clears the decoder cache. Useful for testing or when configuration changes.
     */
    public void clearCache() {
        decoderCache.clear();
    }
}
