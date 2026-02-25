package org.websoso.WSSServer.notification.dto;

import java.util.List;

public record NotificationsGetResponse(
        Boolean isLoadable,
        List<NotificationInfo> notifications
) {
    static public NotificationsGetResponse of(Boolean isLoadable, List<NotificationInfo> notifications) {
        return new NotificationsGetResponse(
                isLoadable,
                notifications
        );
    }
}
