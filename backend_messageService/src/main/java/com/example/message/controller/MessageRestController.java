package com.example.message.controller;

import com.example.message.entity.Message;
import com.example.message.jwt.JwtUtil;
import com.example.message.repo.MessageRepository;
import com.example.message.service.ConnectedUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:5173")  // allow your React app
public class MessageRestController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔹 NEW: to push delete events over WS
    @Autowired
    private ConnectedUserService connectedUserService;

    // 🔹 NEW: to build JSON payload for WS
    @Autowired
    private ObjectMapper objectMapper;

    // 🔹 UPDATED: filter out messages deleted for me
    @GetMapping
    public List<Message> getMyMessages(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        // All messages where I'm sender or receiver
        List<Message> all = messageRepository.findBySenderOrReceiver(email, email);

        // Only those not deleted for me
        List<Message> visible = new ArrayList<>();
        for (Message m : all) {
            if (email.equals(m.getSender()) && !m.isSenderDeleted()) {
                visible.add(m);
            } else if (email.equals(m.getReceiver()) && !m.isReceiverDeleted()) {
                visible.add(m);
            }
        }

        return visible;
    }

    // 🔹 NEW: double tap -> delete only for me (soft delete)
    @DeleteMapping("/{id}/me")
    public void deleteMessageForMe(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        Message msg = messageRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        // mark deleted for this user
        if (email.equals(msg.getSender())) {
            msg.setSenderDeleted(true);
        } else if (email.equals(msg.getReceiver())) {
            msg.setReceiverDeleted(true);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant of this message");
        }

        // if both sides deleted -> remove from DB
        if (msg.isSenderDeleted() && msg.isReceiverDeleted()) {
            messageRepository.deleteById(id);
        } else {
            messageRepository.save(msg);
        }
    }

    // 🔹 NEW: triple tap -> delete for both ends + notify over WebSocket
    @DeleteMapping("/{id}/both")
    public void deleteMessageForBoth(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        Message msg = messageRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        // Rule: only sender can delete for both (change if you want)
        if (!email.equals(msg.getSender())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sender can delete for both");
        }

        String sender = msg.getSender();
        String receiver = msg.getReceiver();

        // remove from DB completely
        messageRepository.deleteById(id);

        // build a small delete event payload: { "type": "DELETE_BOTH", "messageId": "<id>" }
        Map<String, String> payload = new HashMap<>();
        payload.put("type", "DELETE_BOTH");
        payload.put("messageId", id);

        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage wsMessage = new TextMessage(json);

            // send to both sender and receiver if they are connected
            for (String userEmail : new String[]{sender, receiver}) {
                Set<WebSocketSession> sessions = connectedUserService.getSessions(userEmail);
                for (WebSocketSession session : sessions) {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(wsMessage);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
