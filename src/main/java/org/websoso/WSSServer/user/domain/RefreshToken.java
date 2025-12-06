package org.websoso.WSSServer.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 7 * 2)
public class RefreshToken {

    @Id
    private String refreshToken;

    @Indexed
    private Long userId;
}
