package com.example.minishop.config;

import com.example.minishop.constant.CacheNames;
import com.example.minishop.dto.response.ProductResponse;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {

        RedisSerializer<Object> jsonSerializer =
                RedisSerializer.json();

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration
                        .defaultCacheConfig()
                        .disableCachingNullValues()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeKeysWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()
                                        )
                        )
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                jsonSerializer
                                        )
                        );

        JavaType recommendationType =
                TypeFactory.createDefaultInstance()
                        .constructCollectionType(
                                List.class,
                                ProductResponse.class
                        );

        RedisSerializer<List<ProductResponse>>
                recommendationSerializer =
                new JacksonJsonRedisSerializer<>(
                        recommendationType
                );

        RedisCacheConfiguration recommendationConfig =
                defaultConfig
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(
                                RedisSerializationContext
                                        .SerializationPair
                                        .fromSerializer(
                                                recommendationSerializer
                                        )
                        );

        Map<String, RedisCacheConfiguration> configs =
                new HashMap<>();

        configs.put(
                CacheNames.PRODUCT_DETAIL,
                defaultConfig.entryTtl(Duration.ofMinutes(1))
        );

        configs.put(
                CacheNames.PUBLIC_PRODUCT_DETAIL,
                defaultConfig.entryTtl(Duration.ofMinutes(1))
        );

        configs.put(
                CacheNames.SHOP_DETAIL,
                defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        configs.put(
                CacheNames.CATEGORY_LIST,
                defaultConfig.entryTtl(Duration.ofHours(1))
        );

        configs.put(
                CacheNames.VOUCHER_DETAIL,
                defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        configs.put(
                CacheNames.ACTIVE_FLASH_SALE,
                defaultConfig.entryTtl(Duration.ofSeconds(30))
        );

        configs.put(
                CacheNames.USER_RECOMMENDATIONS,
                recommendationConfig
        );

        return RedisCacheManager
                .builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .transactionAware()
                .build();
    }
}