package org.websoso.WSSServer.notification.controller.request;

import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.COMPLETION_NOTIFICATION_NOT_NULL;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.HIATUS_RETURN_NOTIFICATION_NOT_NULL;

import jakarta.validation.constraints.NotNull;

/** 작품 상세에서 변경할 두 작품 알림의 목표 상태를 전달한다. */
public record NovelNotificationUpdateRequest(
        @NotNull(message = COMPLETION_NOTIFICATION_NOT_NULL)
        Boolean isCompletionNotificationEnabled,
        @NotNull(message = HIATUS_RETURN_NOTIFICATION_NOT_NULL)
        Boolean isHiatusReturnNotificationEnabled
) {
}
