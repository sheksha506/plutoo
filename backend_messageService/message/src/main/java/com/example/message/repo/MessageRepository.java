package com.example.message.repo;

import com.example.message.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByReceiverAndDeliveredFalse(String receiver);
    List<Message> findBySenderOrReceiver(String sender, String receiver);
}
