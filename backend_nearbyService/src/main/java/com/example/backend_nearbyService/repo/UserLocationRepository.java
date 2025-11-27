package com.example.backend_nearbyService.repo;

import com.example.backend_nearbyService.document.UserLocation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLocationRepository extends ElasticsearchRepository<UserLocation, Long> {

    /**
     * Find users whose location is within a given distance from a point.
     * @param location center point
     * @param distance maximum distance (e.g., new Distance(10, Metrics.KILOMETERS))
     * @return list of nearby UserLocation
     */
    List<UserLocation> findByLocationNear(GeoPoint location, Distance distance);

}
