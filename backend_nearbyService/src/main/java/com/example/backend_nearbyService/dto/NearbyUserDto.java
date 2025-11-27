package com.example.backend_nearbyService.dto;

public class NearbyUserDto {
    private Long userId;
    private String username;
    private double distanceInKm;

    public NearbyUserDto() {}

    public NearbyUserDto(Long userId, String username, double distanceInKm) {
        this.userId = userId;
        this.username = username;
        this.distanceInKm = distanceInKm;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public double getDistanceInKm() { return distanceInKm; }
    public void setDistanceInKm(double distanceInKm) { this.distanceInKm = distanceInKm; }
}
