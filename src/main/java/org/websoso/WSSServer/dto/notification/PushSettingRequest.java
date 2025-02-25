package org.websoso.WSSServer.dto.notification;

import jakarta.validation.constraints.NotNull;

public record PushSettingRequest(
        @NotNull(message = "푸시 알림 설정 값은 null일 수 없습니다.")
        Boolean isPushEnabled
) {
}
