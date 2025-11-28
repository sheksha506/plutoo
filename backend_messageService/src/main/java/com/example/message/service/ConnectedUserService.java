package com.example.message.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConnectedUserService {

    // email -> set of sessions
    private final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void addSession(String email, WebSocketSession session) {
        userSessions
                .computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet())
                .add(session);
        System.out.println("Connected users: " + userSessions.keySet());
    }

    public void removeSession(String email, WebSocketSession session) {
        Set<WebSocketSession> sessions = userSessions.get(email);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(email);
            }
        }
        System.out.println("Connected users: " + userSessions.keySet());
    }

    public boolean isUserConnected(String email) {
        Set<WebSocketSession> sessions = userSessions.get(email);
        return sessions != null && sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    public Set<WebSocketSession> getSessions(String email) {
        return userSessions.getOrDefault(email, Collections.emptySet());
    }
}
