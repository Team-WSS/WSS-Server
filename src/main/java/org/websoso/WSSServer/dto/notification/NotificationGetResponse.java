package org.websoso.WSSServer.dto.notification;

import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.domain.Notification;

public record NotificationGetResponse(
        String notificationTitle,
        String notificationCreatedDate,
        String notificationDescription
) {
    private static final String DATE_PATTERN = "yyyy.MM.dd";

    static public NotificationGetResponse of(Notification notification) {
        return new NotificationGetResponse(
                notification.getNotificationTitle(),
                notification.getCreatedDate().format(DateTimeFormatter.ofPattern(DATE_PATTERN)),
                notification.getNotificationContent()
        );
    }
}
