package com.example.backend_nearbyService.service;

import com.example.backend_nearbyService.document.UserLocation;
import com.example.backend_nearbyService.dto.NearbyUserDto;

import java.util.List;

public interface NearbyUserService {
    List<NearbyUserDto> getNearbyUsers(double latitude, double longitude, double distanceKm);
    UserLocation saveOrUpdate(UserLocation user);
}
