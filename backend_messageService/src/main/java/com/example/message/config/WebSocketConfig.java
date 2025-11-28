package com.example.message.config;

import com.example.message.controller.WebSocketChatHandler;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtHandShakeInterceptor jwtInterceptor;
    private final WebSocketChatHandler chatHandler;

    public WebSocketConfig(JwtHandShakeInterceptor jwtInterceptor,
                           WebSocketChatHandler chatHandler) {
        this.jwtInterceptor = jwtInterceptor;
        this.chatHandler = chatHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws")
                .setHandshakeHandler(new CustomHandShakeHandler())
                .addInterceptors(jwtInterceptor)
                .setAllowedOriginPatterns("*");
        // NOTE: no withSockJS(), no STOMP endpoints
    }
}
