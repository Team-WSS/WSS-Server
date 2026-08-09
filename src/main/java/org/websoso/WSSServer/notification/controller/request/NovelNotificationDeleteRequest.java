package org.websoso.WSSServer.notification.controller.request;

import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOTIFICATION_TYPE_NOT_NULL;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_IDS_MAX_SIZE;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_IDS_NOT_EMPTY;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_ID_NOT_NULL;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_ID_POSITIVE;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;

/** 설정 화면에서 삭제할 작품 알림 유형과 작품 목록을 전달한다. */
public record NovelNotificationDeleteRequest(
        @NotNull(message = NOTIFICATION_TYPE_NOT_NULL)
        NovelNotificationType notificationType,
        @NotEmpty(message = NOVEL_IDS_NOT_EMPTY)
        @Size(max = 100, message = NOVEL_IDS_MAX_SIZE)
        List<
                @NotNull(message = NOVEL_ID_NOT_NULL)
                @Positive(message = NOVEL_ID_POSITIVE)
                Long> novelIds
) {
}
