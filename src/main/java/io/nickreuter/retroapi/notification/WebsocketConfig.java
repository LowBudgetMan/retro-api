package io.nickreuter.retroapi.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.*;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static org.springframework.messaging.simp.SimpMessageType.MESSAGE;
import static org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocketSecurity
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
//                .nullDestMatcher().authenticated()
                .simpSubscribeDestMatchers("/topic/*/thoughts").access((authentication, object) -> {
                    System.out.println(authentication.toString());
                    System.out.println(authentication.get().getAuthorities());
                    System.out.println(authentication.get().getCredentials());
                    System.out.println(authentication.get().getDetails());
                    System.out.println(authentication.get().getName());
                    System.out.println(authentication.get().getPrincipal());
                    System.out.println(object.getVariables());
                    System.out.println(object.getMessage());
                    return new AuthorizationDecision(true);
                })
                .simpMessageDestMatchers("*").access(((authentication, object) -> {
                    System.out.println(authentication.toString());
                    System.out.println(authentication.get().getAuthorities());
                    System.out.println(authentication.get().getCredentials());
                    System.out.println(authentication.get().getDetails());
                    System.out.println(authentication.get().getName());
                    System.out.println(authentication.get().getPrincipal());
                    System.out.println(object.getVariables());
                    System.out.println(object.getMessage());
                    return new AuthorizationDecision(true);
                }))
//                .simpSubscribeDestMatchers("/user/queue/errors").permitAll()
//                .simpDestMatchers("/app/**").hasRole("USER")
//                .simpSubscribeDestMatchers("/user/**", "/topic/friends/*").hasRole("USER")
//                .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
//                .anyMessage().denyAll()
                .anyMessage().permitAll()
                ;
        return messages.build();
    }

//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
//    }

//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        AuthorizationManager<Message<?>> myAuthorizationRules = AuthenticatedAuthorizationManager.authenticated();
//        AuthorizationChannelInterceptor authz = new AuthorizationChannelInterceptor(myAuthorizationRules);
//        AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(this.context);
//        authz.setAuthorizationEventPublisher(publisher);
//        registration.interceptors(new SecurityContextChannelInterceptor(), authz);
//    }

    @Bean(name = "csrfChannelInterceptor")
    ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {};
    }
}

