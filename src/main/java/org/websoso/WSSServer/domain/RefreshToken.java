package org.websoso.WSSServer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7 * 2)
public class RefreshToken {

    @Id
    private String refreshToken;

    private Long userId;
}
