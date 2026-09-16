package org.websoso.WSSServer.auth.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserInfo(
        Long id,
        Properties properties,
        KakaoAccount kakaoAccount
) {

    public record Properties(@SensitiveData(MaskingPolicy.NAME) String nickname) {
    }

    public record KakaoAccount(@SensitiveData(MaskingPolicy.EMAIL) String email) {
    }

    public String nickname() {
        return properties.nickname();
    }

    public String email() {
        return kakaoAccount.email();
    }
}
