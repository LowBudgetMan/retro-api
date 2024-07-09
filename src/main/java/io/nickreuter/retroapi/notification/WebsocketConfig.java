package io.nickreuter.retroapi.notification;

import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.springframework.messaging.simp.SimpMessageType.*;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocketSecurity
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtDecoder jwtDecoder;
    private final RetroAuthorizationService retroAuthorizationService;
    private final BrokerRelayProperties relayProperties;

    public WebsocketConfig(JwtDecoder jwtDecoder, RetroAuthorizationService retroAuthorizationService, BrokerRelayProperties relayProperties) {
        this.jwtDecoder = jwtDecoder;
        this.retroAuthorizationService = retroAuthorizationService;
        this.relayProperties = relayProperties;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setPathMatcher(new AntPathMatcher("."));
        if(relayProperties.isConfigured()) {
            registry.setApplicationDestinationPrefixes("/topic")
                    .enableStompBrokerRelay("/topic")
                    .setRelayHost(relayProperties.relayHost())
                    .setRelayPort(relayProperties.relayPort())
                    .setClientLogin(relayProperties.relayUsername())
                    .setClientPasscode(relayProperties.relayPassword());
        } else {
            registry.enableStompBrokerRelay("/topic");
        }
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .nullDestMatcher().authenticated()
                .simpSubscribeDestMatchers("/topic/*.thoughts").access((authentication, object) -> {
                    var result = new AuthorizationDecision(false);
                    var destination = Optional.ofNullable((String) object.getMessage().getHeaders().get("simpDestination")).orElse("");
                    var ids = Pattern.compile("^/topic/(?<retroId>.*)\\.thoughts$").matcher(destination);
                    if (ids.find()) {
                        result = new AuthorizationDecision(retroAuthorizationService.isUserAllowedInRetro(
                                authentication.get(),
                                UUID.fromString(ids.group("retroId")))
                        );
                    }
                    return result;
                })
                .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
                .simpTypeMatchers(UNSUBSCRIBE, DISCONNECT).permitAll()
                .anyMessage().denyAll();
        return messages.build();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    var token = String.valueOf(accessor.getFirstNativeHeader("Authorization")).substring(7);
                    var provider = new JwtAuthenticationProvider(jwtDecoder);
                    var authentication = provider.authenticate(new BearerTokenAuthenticationToken(token));
                    accessor.setUser(authentication);
                }
                return message;
            }
        });
    }

    // TODO: Look into CSRF, and why we'd want it for the websocket here
    @Bean(name = "csrfChannelInterceptor")
    ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {};
    }
}

