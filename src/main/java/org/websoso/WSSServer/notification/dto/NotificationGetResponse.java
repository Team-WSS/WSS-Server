package org.websoso.WSSServer.notification.dto;

import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.notification.domain.Notification;

public record NotificationGetResponse(
        String notificationTitle,
        String notificationCreatedDate,
        String notificationDetail
) {
    private static final String DATE_PATTERN = "yyyy.MM.dd";

    static public NotificationGetResponse of(Notification notification) {
        return new NotificationGetResponse(
                notification.getNotificationTitle(),
                notification.getCreatedDate().format(DateTimeFormatter.ofPattern(DATE_PATTERN)),
                notification.getNotificationDetail()
        );
    }
}
