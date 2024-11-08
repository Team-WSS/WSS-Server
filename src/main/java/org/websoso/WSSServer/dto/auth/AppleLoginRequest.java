package org.websoso.WSSServer.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank(message = "애플 토큰 값은 null 이거나, 공백일 수 없습니다.")
        String appleToken
) {
}
