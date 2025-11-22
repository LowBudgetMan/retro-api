package io.nickreuter.retroapi.configuration;

import io.nickreuter.retroapi.configuration.environment.CorsConfig;
import io.nickreuter.retroapi.configuration.jwt.AllTypeJwtDecoderFactory;
import io.nickreuter.retroapi.configuration.jwt.UniversalJwtDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final CorsConfig corsConfig;
    private final AllTypeJwtDecoderFactory decoderFactory;

    public SecurityConfig(CorsConfig corsConfig, AllTypeJwtDecoderFactory decoderFactory) {
        this.corsConfig = corsConfig;
        this.decoderFactory = decoderFactory;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/h2/**").permitAll();
                    authorize.requestMatchers("/api/configuration").permitAll();
                    authorize.requestMatchers("/api/websocket/**").permitAll();
                    authorize.requestMatchers("/api/websocket").permitAll();
                    authorize.anyRequest().authenticated();
                })
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(jwt -> {
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
                }));
        return http.build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsConfig.allowedOrigins().toArray(new String[0]))
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true).maxAge(3600);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Create a simple custom JWT decoder that uses our AllTypeJwtDecoderFactory
        // This decoder will handle all JWT types (JWT, at+jwt, etc.) automatically
        return new UniversalJwtDecoder(decoderFactory);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }
}
