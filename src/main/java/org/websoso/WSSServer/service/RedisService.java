package org.websoso.WSSServer.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisBlackListTemplate;

    public String getValue(String key) {
        ValueOperations<String, Object> value = redisBlackListTemplate.opsForValue();
        if (value.get(key) == null) {
            return null;
        }
        return String.valueOf(value.get(key));
    }

    public void setBlackList(String key, Object o, Long expirationTime) {
        redisBlackListTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(o.getClass()));
        redisBlackListTemplate.opsForValue().set(key, o, expirationTime, MILLISECONDS);
    }

    public boolean hasKeyBlackList(String key) {
        return Boolean.TRUE.equals(redisBlackListTemplate.hasKey(key));
    }
}
