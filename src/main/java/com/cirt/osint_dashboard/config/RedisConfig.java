package com.cirt.osint_dashboard.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Cache Configuration for OSINT Dashboard
 * Compatible with Spring Boot 4.0
 *
 * Uses JDK Serialization (simple and stable)
 * No Jackson dependencies needed!
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configure Redis Template for manual cache operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JDK serialization for values (simple, no Jackson needed)
        RedisSerializer<Object> jdkSerializer = RedisSerializer.java();
        template.setValueSerializer(jdkSerializer);
        template.setHashValueSerializer(jdkSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure Cache Manager with multiple cache zones
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Use JDK serialization (no Jackson needed!)
        RedisSerializer<Object> jdkSerializer = RedisSerializer.java();

        // Default cache configuration (15 minutes)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jdkSerializer)
                )
                .disableCachingNullValues();

        // Build cache manager with multiple cache zones
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("nameSearches",
                        defaultConfig.entryTtl(Duration.ofMinutes(2))) // Name searches cached 20 min
                .withCacheConfiguration("phoneSearches",
                        defaultConfig.entryTtl(Duration.ofMinutes(30))) // Phone searches cached 30 min
                .withCacheConfiguration("emailSearches",
                        defaultConfig.entryTtl(Duration.ofMinutes(30))) // Email searches cached 30 min
                .withCacheConfiguration("addressSearches",
                        defaultConfig.entryTtl(Duration.ofMinutes(10))) // Address searches cached 10 min
                .transactionAware()
                .build();
    }
}