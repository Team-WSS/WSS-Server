package org.websoso.WSSServer.notification.controller.response;

import java.util.Set;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;

/** 작품 상세에서 표시할 완결·휴재 복귀 알림의 현재 상태를 반환한다. */
public record NovelNotificationResponse(
        boolean isCompletionNotificationEnabled,
        boolean isHiatusReturnNotificationEnabled
) {

    /** 등록된 알림 유형 집합을 두 토글 상태로 변환한다. */
    public static NovelNotificationResponse from(Set<NovelNotificationType> notificationTypes) {
        return new NovelNotificationResponse(
                notificationTypes.contains(NovelNotificationType.COMPLETION),
                notificationTypes.contains(NovelNotificationType.HIATUS_RETURN)
        );
    }
}
