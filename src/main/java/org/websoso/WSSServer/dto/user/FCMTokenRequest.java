package org.websoso.WSSServer.dto.user;

import jakarta.validation.constraints.NotBlank;

public record FCMTokenRequest(
        @NotBlank(message = "FCM Token 값은 null 이거나, 공백일 수 없습니다.")
        String fcmToken
) {
}
