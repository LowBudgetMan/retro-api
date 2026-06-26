package io.nickreuter.retroapi.configuration.jwt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AllTypeJwtDecoderFactoryTest {

    @Test
    void resolveIssuerUrl_WithMatchingOverride_RewritesBaseUrl() {
        var overrides = new JwtIssuerOverridesConfig(List.of(
                new JwtIssuerOverridesConfig.IssuerOverride("http://localhost:8010", "http://auth-server:8080")
        ));
        var factory = new AllTypeJwtDecoderFactory(overrides);

        var resolved = factory.resolveIssuerUrl("http://localhost:8010/realms/myrealm");

        assertThat(resolved).isEqualTo("http://auth-server:8080/realms/myrealm");
    }

    @Test
    void resolveIssuerUrl_WithNoMatchingOverride_ReturnsOriginal() {
        var overrides = new JwtIssuerOverridesConfig(List.of(
                new JwtIssuerOverridesConfig.IssuerOverride("http://localhost:8010", "http://auth-server:8080")
        ));
        var factory = new AllTypeJwtDecoderFactory(overrides);

        var resolved = factory.resolveIssuerUrl("http://some-other-provider.com/realms/myrealm");

        assertThat(resolved).isEqualTo("http://some-other-provider.com/realms/myrealm");
    }

    @Test
    void resolveIssuerUrl_WithEmptyOverrides_ReturnsOriginal() {
        var overrides = new JwtIssuerOverridesConfig(List.of());
        var factory = new AllTypeJwtDecoderFactory(overrides);

        var resolved = factory.resolveIssuerUrl("http://localhost:8010/realms/myrealm");

        assertThat(resolved).isEqualTo("http://localhost:8010/realms/myrealm");
    }
}
