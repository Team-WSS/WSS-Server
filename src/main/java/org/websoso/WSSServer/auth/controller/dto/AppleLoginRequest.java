package org.websoso.WSSServer.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

public record AppleLoginRequest(
        @NotBlank(message = "애플 인증 코드 값은 null 이거나, 공백일 수 없습니다.")
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String authorizationCode,
        @NotBlank(message = "애플 ID 토큰 값은 null 이거나, 공백일 수 없습니다.")
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String idToken
) {
}
