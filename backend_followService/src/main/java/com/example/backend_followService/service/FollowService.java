package com.example.backend_followService.service;

import com.example.backend_followService.dto.FollowDto;
import com.example.backend_followService.entity.Follow;

import java.util.List;

public interface FollowService {

    Follow sendFollowRequest(FollowDto followDto);
    String deleteFollowRequest(Long senderId, Long receiverId);
    FollowDto getStatus(FollowDto dto);
    FollowDto acceptRequest(Long senderId, Long receiverId);
    FollowDto rejectRequest(Long senderId, Long receiverId);
    List<FollowDto> getPending(Long receiverId);

    // NEW: Get friends for a user
    List<FollowDto> getFriends(Long userId);
}
