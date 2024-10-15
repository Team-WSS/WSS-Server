package org.websoso.WSSServer.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AppleLoginRequest(
        @NotBlank(message = "사용자 고유 식별자는 null 이거나, 공백일 수 없습니다.")
        String userIdentifier,
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "이메일 형식이 올바르지 않습니다.")
        String email
) {
}
