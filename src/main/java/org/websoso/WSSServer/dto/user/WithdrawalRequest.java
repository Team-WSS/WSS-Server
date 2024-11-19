package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WithdrawalRequest(
        @Size(max = 80, message = "탈퇴 사유는 80자를 초과할 수 없습니다.")
        String reason,
        @NotBlank(message = "리프레시 토큰은 null 이거나, 공백일 수 없습니다.")
        String refreshToken
) {
}
