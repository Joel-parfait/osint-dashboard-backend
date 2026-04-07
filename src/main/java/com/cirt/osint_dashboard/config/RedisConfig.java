package com.cirt.osint_dashboard.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport; // Ajouté
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler; // Ajouté
import org.springframework.cache.interceptor.SimpleCacheErrorHandler; // Ajouté
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
 * Redis Cache Configuration for OSINT Dashboard - CIRT Edition
 * Inclut la gestion du Fallback (si Redis tombe, l'app continue).
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport { // Extension pour le ErrorHandler

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        RedisSerializer<Object> jdkSerializer = RedisSerializer.java();
        template.setValueSerializer(jdkSerializer);
        template.setHashValueSerializer(jdkSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> jdkSerializer = RedisSerializer.java();

        // Configuration par défaut (15 minutes)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jdkSerializer)
                )
                .disableCachingNullValues();

        // Build avec zones spécifiques et support des transactions
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // NOUVEAU : Zone pour la recherche globale (Phase 2)
                .withCacheConfiguration("globalSearches", defaultConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("nameSearches", defaultConfig.entryTtl(Duration.ofMinutes(20)))
                .withCacheConfiguration("phoneSearches", defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("emailSearches", defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("addressSearches", defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .transactionAware()
                .build();
    }

    /**
     * CRUCIAL POUR LA PHASE 1 (Stabilisation) : 
     * Si Redis est injoignable, on logue l'erreur mais on ne crash pas l'app (ERREUR 500 évitée).
     */
    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                System.err.println("⚠️ Redis indisponible (GET) pour le cache " + cache.getName() + ". Fallback vers MongoDB...");
            }
            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                System.err.println("⚠️ Redis indisponible (PUT). L'écriture en cache est ignorée.");
            }
        };
    }
} 