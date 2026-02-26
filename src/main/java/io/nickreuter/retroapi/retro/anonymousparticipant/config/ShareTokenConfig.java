package io.nickreuter.retroapi.retro.anonymousparticipant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
public class ShareTokenConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
