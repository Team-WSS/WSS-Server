package org.websoso.WSSServer.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AppleLogoutRequest(
        @NotBlank(message = "리프레쉬 토큰은 null 이거나, 공백일 수 없습니다.")
        String refreshToken
) {
}
