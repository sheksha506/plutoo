package com.example.backend_nearbyService.controller;

import com.example.backend_nearbyService.document.UserLocation;
import com.example.backend_nearbyService.dto.NearbyUserDto;
import com.example.backend_nearbyService.jwt.JwtUtil;
import com.example.backend_nearbyService.service.NearbyUserService;
import com.example.backend_nearbyService.service.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/nearby")
public class NearbyUserController {

    @Autowired
    private NearbyUserService nearbyUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserClient userClient;

    @PostMapping("/updateLocation")
    public UserLocation update(@RequestHeader("Authorization") String authHeader,
                               @RequestBody LocationUpdateRequest request) {

        String token = authHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);

        Long userId = userClient.getUserIdByEmail(email);

        UserLocation userLocation = new UserLocation();
        userLocation.setUserId(userId);
        userLocation.setUsername(request.getUsername());  // FIXED
        userLocation.setLocation(request.toGeoPoint());

        return nearbyUserService.saveOrUpdate(userLocation);
    }

    @GetMapping
    public List<NearbyUserDto> getNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") double distanceKm
    ) {
        return nearbyUserService.getNearbyUsers(lat, lon, distanceKm);
    }

    public static class LocationUpdateRequest {

        private String username;
        private double lat;
        private double lon;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }

        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }

        public org.springframework.data.elasticsearch.core.geo.GeoPoint toGeoPoint() {
            return new org.springframework.data.elasticsearch.core.geo.GeoPoint(lat, lon);
        }
    }
}
