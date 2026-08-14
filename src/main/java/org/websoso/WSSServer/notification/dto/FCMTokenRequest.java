package org.websoso.WSSServer.notification.dto;

import jakarta.validation.constraints.NotBlank;
import org.websoso.support.logging.masking.MaskingPolicy;
import org.websoso.support.logging.masking.SensitiveData;

public record FCMTokenRequest(
        @NotBlank(message = "FCM Token 값은 null 이거나, 공백일 수 없습니다.")
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String fcmToken,

        @NotBlank(message = "디바이스 식별자 값 null 이거나, 공백일 수 없습니다.")
        @SensitiveData(MaskingPolicy.CREDENTIAL)
        String deviceIdentifier
) {
}
