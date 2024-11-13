package org.websoso.WSSServer.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank(message = "애플 인증 코드 값은 null 이거나, 공백일 수 없습니다.")
        String authorizationCode,
        @NotBlank(message = "애플 ID 토큰 값은 null 이거나, 공백일 수 없습니다.")
        String idToken
) {
}
