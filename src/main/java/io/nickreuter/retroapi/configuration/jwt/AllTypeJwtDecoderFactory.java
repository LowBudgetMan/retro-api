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

@Component
public class AllTypeJwtDecoderFactory {

    private final Map<String, JwtDecoder> decoderCache = new ConcurrentHashMap<>();
    private final JwtIssuerOverridesConfig overridesConfig;

    private static final Set<JOSEObjectType> ALLOWED_JWT_TYPES = new HashSet<>(Arrays.asList(
            JOSEObjectType.JWT,
            new JOSEObjectType("at+jwt"),
            new JOSEObjectType("id+token"),
            new JOSEObjectType("access_token"),
            new JOSEObjectType("RefreshToken"),
            new JOSEObjectType("OpenID Connect"),
            new JOSEObjectType("application/jwt"),
            new JOSEObjectType("application/at+jwt"),
            new JOSEObjectType("application/oauth-jwt")
    ));

    public AllTypeJwtDecoderFactory(JwtIssuerOverridesConfig overridesConfig) {
        this.overridesConfig = overridesConfig;
    }

    public JwtDecoder createDecoder(String issuerUri) {
        return decoderCache.computeIfAbsent(issuerUri, this::createNewDecoder);
    }

    public String resolveIssuerUrl(String issuerUri) {
        for (var entry : overridesConfig.issuerOverrides().entrySet()) {
            if (issuerUri.startsWith(entry.getKey())) {
                return issuerUri.replace(entry.getKey(), entry.getValue());
            }
        }
        return issuerUri;
    }

    private JwtDecoder createNewDecoder(String issuerUri) {
        String fetchUrl = resolveIssuerUrl(issuerUri);
        try {
            return NimbusJwtDecoder.withIssuerLocation(fetchUrl)
                    .jwtProcessorCustomizer(customizer -> {
                        customizer.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(ALLOWED_JWT_TYPES));
                    })
                    .build();
        } catch (Exception e) {
            return createFallbackDecoder(fetchUrl);
        }
    }

    private JwtDecoder createFallbackDecoder(String issuerUri) {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri)
                .jwtProcessorCustomizer(customizer -> {
                    Set<JOSEObjectType> permissiveTypes = new HashSet<>();
                    permissiveTypes.add(new JOSEObjectType(""));
                    permissiveTypes.add(JOSEObjectType.JWT);
                    permissiveTypes.add(new JOSEObjectType("at+jwt"));
                    customizer.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(permissiveTypes));
                })
                .build();
    }

    public void clearCache() {
        decoderCache.clear();
    }
}
