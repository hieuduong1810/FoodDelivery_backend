package com.example.FoodDelivery.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class RobotsController {

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getRobotsTxt() {
        // Auto-detect base URL from request (works with localhost and production)
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        StringBuilder robots = new StringBuilder();

        // Allow all crawlers
        robots.append("User-agent: *\n");
        robots.append("Allow: /\n\n");

        // Disallow admin and API endpoints
        robots.append("Disallow: /api/\n");
        robots.append("Disallow: /admin/\n");
        robots.append("Disallow: /private/\n\n");

        // Sitemap location
        robots.append("Sitemap: ").append(baseUrl).append("/sitemap.xml\n");

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(robots.toString());
    }
}
