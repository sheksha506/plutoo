package com.example.backend_followService.controller;

import com.example.backend_followService.dto.FollowDto;
import com.example.backend_followService.entity.Follow;
import com.example.backend_followService.jwt.JwtUtil;
import com.example.backend_followService.service.FollowService;
import com.example.backend_followService.service.UserClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserClient userClient;

    // Send follow request
    @PostMapping("/request")
    public Follow sendFollowRequest(@RequestBody FollowDto dto,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token); // get logged-in user ID
        long senderId = userClient.getUserIdByEmail(email);
        dto.setSenderId(senderId);

        return followService.sendFollowRequest(dto);
    }

    // Unfollow or delete follow request
    @DeleteMapping("/{receiverId}")
    public String deleteFollow(@PathVariable Long receiverId,
                               @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token); // get logged-in user ID
        long senderId = userClient.getUserIdByEmail(email);
        return followService.deleteFollowRequest(senderId, receiverId);
    }

    // Get follow status for a list of users
    @PostMapping("/statuses")
    public List<FollowDto> getStatuses(@RequestBody List<Long> receiverIds,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token); // get logged-in user ID
        long senderId = userClient.getUserIdByEmail(email);

        return receiverIds.stream().map(rid -> {
            FollowDto dto = new FollowDto();
            dto.setSenderId(senderId);
            dto.setReceiverId(rid);
            return followService.getStatus(dto);
        }).toList();
    }

    // Get pending follow requests received by logged-in user
    @GetMapping("/pendingRequest")
    public List<FollowDto> getPendingRequests(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token); // get logged-in user ID
        long receiverId = userClient.getUserIdByEmail(email);
        return followService.getPending(receiverId);
    }

    // Accept a follow request
    // Accept a follow request
    @PostMapping("/accept")
    public FollowDto accept(@RequestBody FollowDto dto,
                            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        Long receiverId = userClient.getUserIdByEmail(email);

        // Set receiverId from token
        dto.setReceiverId(receiverId);

        // Call service with senderId and receiverId
        return followService.acceptRequest(dto.getSenderId(), receiverId);
    }

    // Reject a follow request
    @PostMapping("/reject")
    public FollowDto reject(@RequestBody FollowDto dto,
                            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        Long receiverId = userClient.getUserIdByEmail(email);

        // Set receiverId from token
        dto.setReceiverId(receiverId);

        // Call service with senderId and receiverId
        return followService.rejectRequest(dto.getSenderId(), receiverId);
    }


    @GetMapping("/friends")
    public List<FollowDto> getFriends(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        Long userId = userClient.getUserIdByEmail(email);

        return followService.getFriends(userId);
    }


}
