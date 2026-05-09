package io.nickreuter.retroapi.notification;

import io.nickreuter.retroapi.retro.RetroAuthorizationService;
import io.nickreuter.retroapi.retro.anonymousparticipant.ShareTokenService;
import io.nickreuter.retroapi.team.apitoken.ApiTokenEntity;
import io.nickreuter.retroapi.team.apitoken.ApiTokenService;
import io.nickreuter.retroapi.team.apitoken.authentication.ApiTokenAuthentication;
import io.nickreuter.retroapi.team.usermapping.UserMappingAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebsocketConfigApiTokenTest {
    private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    private final RetroAuthorizationService retroAuthorizationService = mock(RetroAuthorizationService.class);
    private final BrokerRelayProperties relayProperties = new BrokerRelayProperties(null, 0, null, null);
    private final UserMappingAuthorizationService userMappingAuthorizationService = mock(UserMappingAuthorizationService.class);
    private final ShareTokenService shareTokenService = mock(ShareTokenService.class);
    private final ApiTokenService apiTokenService = mock(ApiTokenService.class);
    private final WebsocketConfig config = new WebsocketConfig(
        jwtDecoder, retroAuthorizationService, relayProperties,
        userMappingAuthorizationService, shareTokenService, apiTokenService
    );

    @Nested
    class ConnectInterceptor {
        private ChannelInterceptor interceptor;

        @BeforeEach
        void setUp() {
            var registration = mock(ChannelRegistration.class);
            when(registration.interceptors(any(ChannelInterceptor.class))).thenAnswer(invocation -> {
                interceptor = invocation.getArgument(0);
                return registration;
            });
            config.configureClientInboundChannel(registration);
        }

        @Test
        void connect_WithValidApiToken_SetsApiTokenAuthentication() {
            var tokenId = UUID.randomUUID();
            var teamId = UUID.randomUUID();
            var entity = new ApiTokenEntity(tokenId, teamId, "Test", "hash", "retro_pat_ab", "read,write", null, "user1", null, null);
            when(apiTokenService.findByTokenString("retro_pat_abcdefg")).thenReturn(Optional.of(entity));

            var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.addNativeHeader("Authorization", "Bearer retro_pat_abcdefg");
            accessor.setSessionAttributes(new HashMap<>());
            accessor.setLeaveMutable(true);
            var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            interceptor.preSend(message, mock(MessageChannel.class));

            assertThat(accessor.getUser()).isInstanceOf(ApiTokenAuthentication.class);
            var auth = (ApiTokenAuthentication) accessor.getUser();
            assertThat(auth.getTeamId()).isEqualTo(teamId);
            assertThat(auth.getScopes()).containsExactlyInAnyOrder("read", "write");
            verify(apiTokenService).touchLastUsed(tokenId);
        }

        @Test
        void connect_WithExpiredApiToken_DoesNotSetAuth() {
            when(apiTokenService.findByTokenString("retro_pat_expired")).thenReturn(Optional.empty());

            var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.addNativeHeader("Authorization", "Bearer retro_pat_expired");
            accessor.setSessionAttributes(new HashMap<>());
            var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            interceptor.preSend(message, mock(MessageChannel.class));

            assertThat(accessor.getUser()).isNull();
            verify(apiTokenService, never()).touchLastUsed(any());
        }

        @Test
        void connect_WithNonApiToken_DoesNotCallApiTokenService() {
            var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
            accessor.addNativeHeader("Authorization", "Bearer eyJhbGciOi...");
            accessor.setSessionAttributes(new HashMap<>());
            var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            interceptor.preSend(message, mock(MessageChannel.class));

            verify(apiTokenService, never()).findByTokenString(any());
        }
    }

    @Nested
    class TeamSubscriptionGuard {
        @Test
        void teamSubscription_WithMatchingApiToken_IsAuthorized() {
            var teamId = UUID.randomUUID();
            var auth = new ApiTokenAuthentication(UUID.randomUUID(), teamId, Set.of("read"));

            var manager = config.messageAuthorizationManager(
                org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.builder()
            );

            var accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination("/topic/teams." + teamId + ".action-items");
            var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            var decision = manager.check(() -> auth, message);
            assertThat(decision.isGranted()).isTrue();
        }

        @Test
        void teamSubscription_WithWrongTeamApiToken_IsDenied() {
            var wrongTeamId = UUID.randomUUID();
            var auth = new ApiTokenAuthentication(UUID.randomUUID(), wrongTeamId, Set.of("read"));

            var manager = config.messageAuthorizationManager(
                org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.builder()
            );

            var realTeamId = UUID.randomUUID();
            var accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination("/topic/teams." + realTeamId + ".action-items");
            var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            var decision = manager.check(() -> auth, message);
            assertThat(decision.isGranted()).isFalse();
        }

        @Test
        void teamSubscription_WithWriteOnlyApiToken_IsDenied() {
            var teamId = UUID.randomUUID();
            var auth = new ApiTokenAuthentication(UUID.randomUUID(), teamId, Set.of("write"));

            var manager = config.messageAuthorizationManager(
                org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager.builder()
            );

            var accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setDestination("/topic/teams." + teamId + ".action-items");
            var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

            var decision = manager.check(() -> auth, message);
            assertThat(decision.isGranted()).isFalse();
        }
    }
}
