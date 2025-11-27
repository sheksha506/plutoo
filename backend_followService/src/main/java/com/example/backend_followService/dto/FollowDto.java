package com.example.backend_followService.dto;

import com.example.backend_followService.enums.FollowStatus;

public class FollowDto {


    private String username;
    private long senderId;
    private long receiverId;
    private FollowStatus followStatus;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public FollowStatus getFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(FollowStatus followStatus) {
        this.followStatus = followStatus;
    }
}
