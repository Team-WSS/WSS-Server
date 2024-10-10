package org.websoso.WSSServer.oauth2.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserInfo(
        Long id,
        Properties properties,
        KakaoAccount kakaoAccount
) {

    public record Properties(String nickname) {
    }

    public record KakaoAccount(String email) {
    }

    public String nickname() {
        return properties.nickname();
    }

    public String email() {
        return kakaoAccount.email();
    }
}
