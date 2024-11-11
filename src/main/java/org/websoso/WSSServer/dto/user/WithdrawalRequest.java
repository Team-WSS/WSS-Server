package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.Size;

public record WithdrawalRequest(
        @Size(max = 80, message = "탈퇴 사유는 80자를 초과할 수 없습니다.")
        String reason,

        String refreshToken
) {
}
