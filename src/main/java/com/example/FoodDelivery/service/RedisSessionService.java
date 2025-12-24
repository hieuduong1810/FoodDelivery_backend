package com.example.FoodDelivery.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisSessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_PREFIX = "session:user:";
    private static final long SESSION_TIMEOUT = 2; // 2 hours

    public RedisSessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Create user session
     */
    public void createSession(Long userId, String token, Map<String, Object> sessionData) {
        try {
            String key = SESSION_PREFIX + userId;
            Map<String, Object> data = new HashMap<>(sessionData);
            data.put("userId", userId);
            data.put("token", token);
            data.put("loginTime", System.currentTimeMillis());

            redisTemplate.opsForHash().putAll(key, data);
            redisTemplate.expire(key, SESSION_TIMEOUT, TimeUnit.HOURS);

            log.info("Created session for user: {}", userId);
        } catch (Exception e) {
            log.error("Error creating session for user: {}", userId, e);
        }
    }

    /**
     * Get user session
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSession(Long userId) {
        try {
            String key = SESSION_PREFIX + userId;
            Map<Object, Object> sessionMap = redisTemplate.opsForHash().entries(key);

            if (sessionMap.isEmpty()) {
                return null;
            }

            // Convert Map<Object, Object> to Map<String, Object>
            Map<String, Object> session = new HashMap<>();
            sessionMap.forEach((k, v) -> session.put(k.toString(), v));

            // Refresh session TTL on access
            redisTemplate.expire(key, SESSION_TIMEOUT, TimeUnit.HOURS);

            return session;
        } catch (Exception e) {
            log.error("Error getting session for user: {}", userId, e);
            return null;
        }
    }

    /**
     * Update session data
     */
    public void updateSession(Long userId, String field, Object value) {
        try {
            String key = SESSION_PREFIX + userId;
            redisTemplate.opsForHash().put(key, field, value);
            redisTemplate.expire(key, SESSION_TIMEOUT, TimeUnit.HOURS);

            log.debug("Updated session field {} for user: {}", field, userId);
        } catch (Exception e) {
            log.error("Error updating session for user: {}", userId, e);
        }
    }

    /**
     * Delete user session (logout)
     */
    public void deleteSession(Long userId) {
        try {
            String key = SESSION_PREFIX + userId;
            redisTemplate.delete(key);
            log.info("Deleted session for user: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting session for user: {}", userId, e);
        }
    }

    /**
     * Check if session exists and valid
     */
    public boolean isSessionValid(Long userId) {
        try {
            String key = SESSION_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking session for user: {}", userId, e);
            return false;
        }
    }

    /**
     * Get session field value
     */
    public Object getSessionField(Long userId, String field) {
        try {
            String key = SESSION_PREFIX + userId;
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Error getting session field for user: {}", userId, e);
            return null;
        }
    }
}
