package com.example.FoodDelivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MapboxService {
    private static final Logger log = LoggerFactory.getLogger(MapboxService.class);
    private static final String MAPBOX_DIRECTIONS_URL = "https://api.mapbox.com/directions/v5/mapbox/driving";

    @Value("${mapbox.access.token}")
    private String mapboxToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MapboxService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get driving distance in kilometers between two points using Mapbox Directions
     * API
     * 
     * @param fromLat Starting point latitude
     * @param fromLng Starting point longitude
     * @param toLat   Destination latitude
     * @param toLng   Destination longitude
     * @return Distance in kilometers, or null if API call fails
     */
    public BigDecimal getDrivingDistance(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng) {
        try {
            // Format: {longitude},{latitude};{longitude},{latitude}
            String coordinates = fromLng + "," + fromLat + ";" + toLng + "," + toLat;

            String url = UriComponentsBuilder
                    .fromHttpUrl(MAPBOX_DIRECTIONS_URL + "/" + coordinates)
                    .queryParam("access_token", mapboxToken)
                    .queryParam("geometries", "geojson")
                    .queryParam("overview", "false")
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                log.warn("Mapbox API returned null response");
                return null;
            }

            // Parse JSON response
            JsonNode root = objectMapper.readTree(response);
            JsonNode routes = root.get("routes");

            if (routes == null || routes.isEmpty()) {
                log.warn("No routes found in Mapbox response");
                return null;
            }

            // Get distance in meters from first route
            JsonNode firstRoute = routes.get(0);
            double distanceInMeters = firstRoute.get("distance").asDouble();

            // Convert to kilometers
            BigDecimal distanceInKm = new BigDecimal(distanceInMeters / 1000.0);

            log.debug("Mapbox distance from ({},{}) to ({},{}): {} km",
                    fromLat, fromLng, toLat, toLng, distanceInKm);

            return distanceInKm;

        } catch (Exception e) {
            log.error("Failed to get driving distance from Mapbox API: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get driving duration in minutes between two points
     * 
     * @param fromLat Starting point latitude
     * @param fromLng Starting point longitude
     * @param toLat   Destination latitude
     * @param toLng   Destination longitude
     * @return Duration in minutes, or null if API call fails
     */
    public BigDecimal getDrivingDuration(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng) {
        try {
            String coordinates = fromLng + "," + fromLat + ";" + toLng + "," + toLat;

            String url = UriComponentsBuilder
                    .fromHttpUrl(MAPBOX_DIRECTIONS_URL + "/" + coordinates)
                    .queryParam("access_token", mapboxToken)
                    .queryParam("geometries", "geojson")
                    .queryParam("overview", "false")
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode routes = root.get("routes");

            if (routes == null || routes.isEmpty()) {
                return null;
            }

            JsonNode firstRoute = routes.get(0);
            double durationInSeconds = firstRoute.get("duration").asDouble();

            // Convert to minutes
            BigDecimal durationInMinutes = new BigDecimal(durationInSeconds / 60.0);

            return durationInMinutes;

        } catch (Exception e) {
            log.error("Failed to get driving duration from Mapbox API: {}", e.getMessage(), e);
            return null;
        }
    }
}
