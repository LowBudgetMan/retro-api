package io.nickreuter.retroapi.notification;

import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
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
import java.util.function.Supplier;
import java.util.regex.Matcher;
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
    private final UserMappingAuthorizationService userMappingAuthorizationService;

    public WebsocketConfig(JwtDecoder jwtDecoder, RetroAuthorizationService retroAuthorizationService, BrokerRelayProperties relayProperties, UserMappingAuthorizationService userMappingAuthorizationService) {
        this.jwtDecoder = jwtDecoder;
        this.retroAuthorizationService = retroAuthorizationService;
        this.relayProperties = relayProperties;
        this.userMappingAuthorizationService = userMappingAuthorizationService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/api")
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
            registry.enableSimpleBroker("/topic");
        }
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .nullDestMatcher().authenticated()
                .simpSubscribeDestMatchers("/topic/*.thoughts").access(this::isAuthorizedRetroSubscription)
                .simpSubscribeDestMatchers("/topic/*.finished").access(this::isAuthorizedRetroSubscription)
                .simpSubscribeDestMatchers("/topic/*.action-items").access((this::isAuthorizedTeamSubscription))
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

    private AuthorizationDecision isAuthorizedRetroSubscription(Supplier<Authentication> authentication, MessageAuthorizationContext<?> object) {
        var ids = getIdFromTopic(object, "^/topic/(?<retroId>.*)\\.thoughts$");
        return ids.find()
                ? new AuthorizationDecision(retroAuthorizationService.isUserAllowedInRetro(authentication.get(), UUID.fromString(ids.group("retroId"))))
                : new AuthorizationDecision(false);
    }

    private AuthorizationDecision isAuthorizedTeamSubscription(Supplier<Authentication> authentication, MessageAuthorizationContext<?> object) {
        var ids = getIdFromTopic(object, "^/topic/(?<teamId>.*)\\..*$");
        AuthorizationDecision isAuthorized = ids.find()
                // TODO: Ad error handling for when this fails because the UUID is too big
                ? new AuthorizationDecision(userMappingAuthorizationService.isUserMemberOfTeam(authentication.get(), UUID.fromString(ids.group("teamId"))))
                : new AuthorizationDecision(false);
        return isAuthorized;
    }

    private Matcher getIdFromTopic(MessageAuthorizationContext<?> object, String regex) {
        var destination = Optional.ofNullable((String) object.getMessage().getHeaders().get("simpDestination")).orElse("");
        return Pattern.compile(regex).matcher(destination);
    }
}
