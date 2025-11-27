package com.example.message.producer;

import com.example.message.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final String TOPIC = "chat-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendMessage(Message message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(TOPIC, json);
            System.out.println("Produced to Kafka: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
