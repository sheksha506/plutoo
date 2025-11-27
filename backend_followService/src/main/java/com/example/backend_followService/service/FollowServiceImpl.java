package com.example.backend_followService.service.impl;

import com.example.backend_followService.dto.FollowDto;
import com.example.backend_followService.entity.Follow;
import com.example.backend_followService.enums.FollowStatus;
import com.example.backend_followService.repo.FollowRepository;
import com.example.backend_followService.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Override
    public Follow sendFollowRequest(FollowDto followDto) {
        Optional<Follow> existingOpt = followRepository.findBySenderIdAndReceiverId(
                followDto.getSenderId(), followDto.getReceiverId());

        if (existingOpt.isEmpty()) {
            Follow follow = new Follow();
            follow.setUsername(followDto.getUsername());
            follow.setSenderId(followDto.getSenderId());
            follow.setReceiverId(followDto.getReceiverId());
            follow.setFollowStatus(FollowStatus.PENDING);
            follow.setDelivered(false);
            return followRepository.save(follow);
        }

        Follow existing = existingOpt.get();
        if (existing.getFollowStatus() == FollowStatus.REJECTED) {
            existing.setFollowStatus(FollowStatus.PENDING);
            existing.setDelivered(false);
            return followRepository.save(existing);
        }

        return existing;
    }

    @Override
    public String deleteFollowRequest(Long senderId, Long receiverId) {
        // Try (sender -> receiver)
        Optional<Follow> existingOpt =
                followRepository.findBySenderIdAndReceiverId(senderId, receiverId);

        // If not found, try the opposite direction (receiver -> sender)
        if (existingOpt.isEmpty()) {
            existingOpt = followRepository.findBySenderIdAndReceiverId(receiverId, senderId);
        }

        if (existingOpt.isPresent()) {
            followRepository.delete(existingOpt.get());
            return "Unfollowed successfully";
        }
        return "No follow request found";
    }


    @Override
    public FollowDto getStatus(FollowDto followDto) {
        // Try as (sender -> receiver)
        Optional<Follow> followOpt =
                followRepository.findBySenderIdAndReceiverId(followDto.getSenderId(), followDto.getReceiverId());

        // If not found, try the opposite direction (receiver -> sender)
        if (followOpt.isEmpty()) {
            followOpt = followRepository.findBySenderIdAndReceiverId(
                    followDto.getReceiverId(), followDto.getSenderId()
            );
        }

        followDto.setFollowStatus(followOpt.map(Follow::getFollowStatus).orElse(FollowStatus.NONE));
        return followDto;
    }

    @Override
    public FollowDto acceptRequest(Long senderId, Long receiverId) {
        Optional<Follow> followOpt = followRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        FollowDto dto = new FollowDto();
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);

        followOpt.ifPresent(follow -> {
            follow.setFollowStatus(FollowStatus.ACCEPTED);
            followRepository.save(follow);
            dto.setFollowStatus(FollowStatus.ACCEPTED);
        });

        return dto;
    }

    @Override
    public FollowDto rejectRequest(Long senderId, Long receiverId) {
        Optional<Follow> followOpt = followRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        FollowDto dto = new FollowDto();
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);

        followOpt.ifPresent(follow -> {
            follow.setFollowStatus(FollowStatus.REJECTED);
            followRepository.save(follow);
            dto.setFollowStatus(FollowStatus.REJECTED);
        });

        return dto;
    }

    @Override
    public List<FollowDto> getPending(Long receiverId) {
        return followRepository.findByReceiverIdAndFollowStatus(receiverId, FollowStatus.PENDING)
                .stream()
                .map(follow -> {
                    FollowDto dto = new FollowDto();
                    dto.setSenderId(follow.getSenderId());
                    dto.setReceiverId(follow.getReceiverId());
                    dto.setUsername(follow.getUsername());
                    dto.setFollowStatus(follow.getFollowStatus());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Override
    public List<FollowDto> getFriends(Long userId) {
        List<Follow> friends = followRepository.findBySenderIdOrReceiverIdAndFollowStatus(userId, userId, FollowStatus.ACCEPTED);

        return friends.stream().map(follow -> {
            FollowDto dto = new FollowDto();
            dto.setFollowStatus(FollowStatus.ACCEPTED);
            dto.setSenderId(follow.getSenderId());
            dto.setReceiverId(follow.getReceiverId());

            // Identify the friend (not logged-in user)
            if (follow.getSenderId() == userId) {
                dto.setUsername(follow.getUsername()); // sent request
            } else {
                dto.setUsername(follow.getUsername()); // received request
            }
            return dto;
        }).collect(Collectors.toList());
    }

}
