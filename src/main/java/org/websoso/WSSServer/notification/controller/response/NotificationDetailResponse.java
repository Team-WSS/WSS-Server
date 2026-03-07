package org.websoso.WSSServer.notification.controller.response;

import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.notification.domain.Notification;

public record NotificationDetailResponse(
        String notificationTitle,
        String notificationCreatedDate,
        String notificationDetail
) {
    private static final String DATE_PATTERN = "yyyy.MM.dd";

    public static NotificationDetailResponse of(Notification notification) {
        return new NotificationDetailResponse(
                notification.getNotificationTitle(),
                notification.getCreatedDate().format(DateTimeFormatter.ofPattern(DATE_PATTERN)),
                notification.getNotificationDetail()
        );
    }
}
