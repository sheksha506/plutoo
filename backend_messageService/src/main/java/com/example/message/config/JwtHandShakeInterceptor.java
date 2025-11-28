package com.example.message.config;

import com.example.message.entity.StompPrincipal;
import com.example.message.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class JwtHandShakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            URI uri = request.getURI();
            String query = uri.getQuery();
            if (query == null) {
                System.out.println("Handshake: no query");
                return false;
            }

            // token may appear as token=<thetoken>, if there are other params parse simply
            String token = null;
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    token = param.substring("token=".length());
                    break;
                }
            }
            if (token == null) {
                System.out.println("Handshake: token missing");
                return false;
            }

            if (!jwtUtil.validtoken(token)) {
                System.out.println("Handshake: invalid token");
                return false;
            }

            String email = jwtUtil.getEmailFromToken(token);
            attributes.put("principal", new StompPrincipal(email)); // <-- correct key
            System.out.println("✅ WebSocket Authenticated: " + email);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
