package com.example.message.consumer;

import com.example.message.entity.Message;
import com.example.message.repo.MessageRepository;
import com.example.message.service.ConnectedUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

@Service
public class KafkaConsumer {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConnectedUserService connectedUserService;

    @KafkaListener(topics = "chat-topic", groupId = "chat-group")
    public void consume(String messageJson) {
        try {
            Message message = objectMapper.readValue(messageJson, Message.class);
            message.setDelivered(false);
            Message savedMessage = messageRepository.save(message);

            System.out.println("KafkaConsumer saved message: " + savedMessage);

            String receiver = savedMessage.getReceiver();

            if (connectedUserService.isUserConnected(receiver)) {
                String wsPayload = objectMapper.writeValueAsString(savedMessage);
                TextMessage wsMessage = new TextMessage(wsPayload);

                Set<WebSocketSession> sessions = connectedUserService.getSessions(receiver);
                for (WebSocketSession session : sessions) {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(wsMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                savedMessage.setDelivered(true);
                messageRepository.save(savedMessage);
                System.out.println("Delivered via WebSocket to: " + receiver);
            } else {
                System.out.println("Receiver not connected: " + receiver);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
