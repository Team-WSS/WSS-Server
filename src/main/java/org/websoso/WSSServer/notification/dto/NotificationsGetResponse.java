package org.websoso.WSSServer.notification.dto;

import java.util.List;
import org.springframework.data.domain.Slice;

public record NotificationsGetResponse(
        Boolean isLoadable,
        List<NotificationInfo> notifications
) {
    public static NotificationsGetResponse of(Boolean isLoadable, List<NotificationInfo> notifications) {
        return new NotificationsGetResponse(
                isLoadable,
                notifications
        );
    }

    public static NotificationsGetResponse from(Slice<ReadNotificationDto> notificationSlice) {
        List<NotificationInfo> notificationInfos = notificationSlice.getContent().stream()
                .map(NotificationInfo::from)
                .toList();

        return new NotificationsGetResponse(
                notificationSlice.hasNext(),
                notificationInfos
        );
    }
}
