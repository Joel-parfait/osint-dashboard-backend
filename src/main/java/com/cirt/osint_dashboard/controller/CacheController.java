package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.service.PersonService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Cache Management Controller
 *
 * Provides endpoints to:
 * - View cache statistics
 * - Clear specific caches
 * - Clear all caches
 * - Monitor cache health
 */
@RestController
@RequestMapping("/api/cache")
//@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CacheController {

    private final CacheManager cacheManager;
    private final PersonService personService;
    private final RedisTemplate<String, Object> redisTemplate;

    public CacheController(
            CacheManager cacheManager,
            PersonService personService,
            RedisTemplate<String, Object> redisTemplate) {
        this.cacheManager = cacheManager;
        this.personService = personService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * GET /api/cache/stats
     * Returns statistics for all caches
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> cacheList = new ArrayList<>();

        // Get stats for each cache zone
        String[] cacheNames = {"nameSearches", "phoneSearches", "emailSearches", "addressSearches"};

        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof RedisCache redisCache) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("name", cacheName);
                cacheInfo.put("type", "Redis");

                // Get cache size (approximate)
                try {
                    Set<String> keys = redisTemplate.keys("osint:" + cacheName + "::*");
                    cacheInfo.put("size", keys != null ? keys.size() : 0);
                    cacheInfo.put("keys", keys != null ? new ArrayList<>(keys) : new ArrayList<>());
                } catch (Exception e) {
                    cacheInfo.put("size", "N/A");
                    cacheInfo.put("error", e.getMessage());
                }

                cacheList.add(cacheInfo);
            }
        }

        stats.put("caches", cacheList);
        stats.put("totalCaches", cacheList.size());
        stats.put("redisConnected", checkRedisConnection());
        stats.put("timestamp", new Date().toString());

        return ResponseEntity.ok(stats);
    }

    /**
     * POST /api/cache/clear
     * Clear all caches
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        try {
            personService.clearAllCaches();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All caches cleared successfully",
                    "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Failed to clear caches: " + e.getMessage()
            ));
        }
    }

    /**
     * POST /api/cache/clear/{type}
     * Clear specific cache by type
     */
    @PostMapping("/clear/{type}")
    public ResponseEntity<Map<String, Object>> clearSpecificCache(@PathVariable String type) {
        try {
            switch (type.toLowerCase()) {
                case "name":
                    personService.clearNameCache();
                    break;
                case "phone":
                    personService.clearPhoneCache();
                    break;
                case "email":
                    personService.clearEmailCache();
                    break;
                case "address":
                    personService.clearAddressCache();
                    break;
                default:
                    return ResponseEntity.ok(Map.of(
                            "success", false,
                            "message", "Invalid cache type. Valid types: name, phone, email, address"
                    ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", type + " cache cleared successfully",
                    "timestamp", new Date().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Failed to clear " + type + " cache: " + e.getMessage()
            ));
        }
    }

    /**
     * GET /api/cache/health
     * Check Redis connection health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        boolean connected = checkRedisConnection();

        return ResponseEntity.ok(Map.of(
                "redis", connected ? "connected" : "disconnected",
                "status", connected ? "healthy" : "unhealthy",
                "timestamp", new Date().toString()
        ));
    }

    /**
     * GET /api/cache/info
     * Get detailed Redis info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            // Get Redis connection info
            Properties redisInfo = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .serverCommands()
                    .info();

            info.put("connected", true);
            info.put("version", redisInfo.getProperty("redis_version", "unknown"));
            info.put("uptime_days", redisInfo.getProperty("uptime_in_days", "unknown"));
            info.put("used_memory_human", redisInfo.getProperty("used_memory_human", "unknown"));
            info.put("total_connections_received", redisInfo.getProperty("total_connections_received", "unknown"));

        } catch (Exception e) {
            info.put("connected", false);
            info.put("error", e.getMessage());
        }

        return ResponseEntity.ok(info);
    }

    /**
     * Helper method to check Redis connection
     */
    private boolean checkRedisConnection() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}