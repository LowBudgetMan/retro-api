package io.nickreuter.retroapi.team.webhook;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureTest {
    @Test
    void sign_ProducesConsistentHmac() {
        var body = "{\"event_type\":\"action_item.created\"}";
        var secret = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";

        var sig1 = WebhookSignature.sign(body, secret);
        var sig2 = WebhookSignature.sign(body, secret);

        assertThat(sig1).isEqualTo(sig2);
        assertThat(sig1).startsWith("sha256=");
        assertThat(sig1.substring(7)).hasSize(64);
    }

    @Test
    void sign_DifferentBodies_ProduceDifferentSignatures() {
        var secret = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
        var sig1 = WebhookSignature.sign("body1", secret);
        var sig2 = WebhookSignature.sign("body2", secret);

        assertThat(sig1).isNotEqualTo(sig2);
    }

    @Test
    void sign_DifferentSecrets_ProduceDifferentSignatures() {
        var body = "same body";
        var sig1 = WebhookSignature.sign(body, "secret1");
        var sig2 = WebhookSignature.sign(body, "secret2");

        assertThat(sig1).isNotEqualTo(sig2);
    }
}
