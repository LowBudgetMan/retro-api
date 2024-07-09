package io.nickreuter.retroapi.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrokerRelayPropertiesTest {

    @Test
    void isConfigured_WhenNoPropertiesConfigured_ReturnsFalse() {
        var properties = new BrokerRelayProperties(null, null, null, null);
        assertFalse(properties.isConfigured());
    }

    @Test
    void isConfigured_WhenHostNotConfigured_ReturnsFalse() {
        var properties = new BrokerRelayProperties(null, 1234, "username", "password");
        assertFalse(properties.isConfigured());
    }

    @Test
    void isConfigured_WhenPortNotConfigured_ReturnsFalse() {
        var properties = new BrokerRelayProperties("host", null, "username", "password");
        assertFalse(properties.isConfigured());
    }

    @Test
    void isConfigured_WhenUsernameNotConfigured_ReturnsFalse() {
        var properties = new BrokerRelayProperties("host", 1234, null, "password");
        assertFalse(properties.isConfigured());
    }

    @Test
    void isConfigured_WhenPasswordNotConfigured_ReturnsFalse() {
        var properties = new BrokerRelayProperties("host", 1234, "username", null);
        assertFalse(properties.isConfigured());
    }

    @Test
    void isConfigured_WhenAllPropertiesAreConfigured_ReturnsTrue() {
        var properties = new BrokerRelayProperties("host", 1234, "username", "password");
        assertTrue(properties.isConfigured());
    }
}