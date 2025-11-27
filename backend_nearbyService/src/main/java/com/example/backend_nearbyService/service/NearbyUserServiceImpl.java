package com.example.backend_nearbyService.service;

import com.example.backend_nearbyService.document.UserLocation;
import com.example.backend_nearbyService.dto.NearbyUserDto;
import com.example.backend_nearbyService.repo.UserLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NearbyUserServiceImpl implements NearbyUserService {

    @Autowired
    private UserLocationRepository userLocationRepository;

    @Autowired
    private ElasticsearchOperations operations;

    @Override
    public List<NearbyUserDto> getNearbyUsers(double lat, double lon, double distanceKm) {
        GeoPoint center = new GeoPoint(lat, lon);

        Criteria criteria = new Criteria("location").within(center, distanceKm + "km");
        Query query = new CriteriaQuery(criteria);

        SearchHits<UserLocation> hits = operations.search(query, UserLocation.class);

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(user -> new NearbyUserDto(
                        user.getUserId(),
                        user.getUsername(),
                        calculateDistance(lat, lon, user.getLocation().getLat(), user.getLocation().getLon())
                ))
                .toList();
    }

    @Override
    public UserLocation saveOrUpdate(UserLocation user) {
        if (user.getLocation() == null) {
            throw new RuntimeException("Location cannot be null");
        }
        return userLocationRepository.save(user);
    }

    // Haversine formula to calculate distance between two points in KM
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
