package org.websoso.WSSServer.dto.user;

public record WithdrawalRequest(
        String reason,

        String refreshToken
) {
}
