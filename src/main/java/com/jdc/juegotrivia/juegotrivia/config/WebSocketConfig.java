package com.jdc.juegotrivia.juegotrivia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para conexión WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permite CORS
                .withSockJS(); // Habilita SockJS como fallback

        System.out.println("✅ WebSocket endpoint configurado en /ws");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para mensajes del servidor a cliente
        registry.enableSimpleBroker("/topic");

        // Prefijo para mensajes del cliente al servidor
        registry.setApplicationDestinationPrefixes("/app");

        System.out.println("✅ Message broker configurado - topic: /topic, app: /app");
    }
}