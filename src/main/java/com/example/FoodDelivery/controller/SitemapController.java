package com.example.FoodDelivery.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.FoodDelivery.domain.DishCategory;
import com.example.FoodDelivery.domain.Restaurant;
import com.example.FoodDelivery.repository.DishCategoryRepository;
import com.example.FoodDelivery.repository.RestaurantRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class SitemapController {

    private final RestaurantRepository restaurantRepository;
    private final DishCategoryRepository dishCategoryRepository;

    public SitemapController(RestaurantRepository restaurantRepository,
            DishCategoryRepository dishCategoryRepository) {
        this.restaurantRepository = restaurantRepository;
        this.dishCategoryRepository = dishCategoryRepository;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getSitemap() {
        StringBuilder sitemap = new StringBuilder();
        // Auto-detect base URL from request (works with localhost and production)
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // XML header
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Homepage
        sitemap.append("  <url>\n");
        sitemap.append("    <loc>").append(baseUrl).append("/</loc>\n");
        sitemap.append("    <lastmod>").append(today).append("</lastmod>\n");
        sitemap.append("    <changefreq>daily</changefreq>\n");
        sitemap.append("    <priority>1.0</priority>\n");
        sitemap.append("  </url>\n");

        // All restaurants
        List<Restaurant> restaurants = restaurantRepository.findAll();
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getSlug() != null && !restaurant.getSlug().isEmpty()) {
                sitemap.append("  <url>\n");
                sitemap.append("    <loc>").append(baseUrl)
                        .append("/restaurant/").append(restaurant.getSlug())
                        .append("-").append(restaurant.getId())
                        .append("</loc>\n");
                sitemap.append("    <lastmod>").append(today).append("</lastmod>\n");
                sitemap.append("    <changefreq>weekly</changefreq>\n");
                sitemap.append("    <priority>0.8</priority>\n");
                sitemap.append("  </url>\n");
            }
        }

        // All dish categories
        List<DishCategory> categories = dishCategoryRepository.findAll();
        for (DishCategory category : categories) {
            if (category.getSlug() != null && !category.getSlug().isEmpty()) {
                sitemap.append("  <url>\n");
                sitemap.append("    <loc>").append(baseUrl)
                        .append("/category/").append(category.getSlug())
                        .append("-").append(category.getId())
                        .append("</loc>\n");
                sitemap.append("    <lastmod>").append(today).append("</lastmod>\n");
                sitemap.append("    <changefreq>weekly</changefreq>\n");
                sitemap.append("    <priority>0.6</priority>\n");
                sitemap.append("  </url>\n");
            }
        }

        sitemap.append("</urlset>");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(sitemap.toString());
    }
}
