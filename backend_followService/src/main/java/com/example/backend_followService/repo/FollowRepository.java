package com.example.backend_followService.repo;

import com.example.backend_followService.entity.Follow;
import com.example.backend_followService.enums.FollowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<Follow> findByReceiverIdAndFollowStatus(Long receiverId, FollowStatus status);

    // Find all accepted follows where the user is either sender or receiver
    List<Follow> findBySenderIdOrReceiverIdAndFollowStatus(Long senderId, Long receiverId, FollowStatus status);
}
