package com.example.message.controller;



import com.example.message.entity.Message;
import com.example.message.producer.KafkaProducer;
import com.example.message.repo.MessageRepository;
import com.example.message.service.ConnectedUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class WebSocketChatHandler extends TextWebSocketHandler {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private ConnectedUserService connectedUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            String email = principal.getName();
            connectedUserService.addSession(email, session);
            System.out.println("WS connected: " + email);
        } else {
            System.out.println("WS connected: anonymous");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            String email = principal.getName();
            connectedUserService.removeSession(email, session);
            System.out.println("WS disconnected: " + email);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal == null) {
            System.out.println("Received message from anonymous, ignoring.");
            return;
        }

        String senderEmail = principal.getName();
        String payload = textMessage.getPayload();

        // Expect JSON like: { "receiver": "b@x.com", "content": "hello" }
        Message message = objectMapper.readValue(payload, Message.class);
        message.setSender(senderEmail);
        message.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
        message.setDelivered(false);

        Message saved = messageRepository.save(message);
        kafkaProducer.sendMessage(saved);

        System.out.println("Saved & produced message: " + saved);
    }
}

