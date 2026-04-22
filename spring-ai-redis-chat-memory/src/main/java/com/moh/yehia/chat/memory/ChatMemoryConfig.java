package com.moh.yehia.chat.memory;

import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

@Configuration
public class ChatMemoryConfig {
    @Value("${spring.ai.chat.memory.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.ai.chat.memory.redis.port:6379}")
    private int redisPort;

    @Value("${spring.ai.chat.memory.redis.index-name:spring-ai-chat-memory}")
    private String indexName;

    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository() {
        JedisPooled jedisClient = new JedisPooled(redisHost, redisPort);

        return RedisChatMemoryRepository.builder()
                .jedisClient(jedisClient)
                .indexName(indexName)
                .keyPrefix("my-chat:")
                .timeToLive(Duration.ofHours(24))
                .build();
    }
}
