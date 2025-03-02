package org.websoso.WSSServer.dto.notification;

import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;

public record NotificationInfo(
        Long notificationId,
        String notificationImage,
        String notificationTitle,
        String notificationBody,
        String createdDate,
        Boolean isRead,
        Boolean isNotice,
        Long feedId
) {

    private static final String JUST_NOW = "방금 전";
    private static final String MINUTE_UNIT = "분 전";
    private static final String HOUR_UNIT = "시간 전";
    private static final String DATE_PATTERN = "yyyy.MM.dd";

    static public NotificationInfo of(Notification notification, Boolean isRead) {
        return new NotificationInfo(
                notification.getNotificationId(),
                notification.getNotificationType().getNotificationTypeImage(),
                notification.getNotificationTitle(),
                notification.getNotificationBody(),
                formatCreatedDate(notification.getCreatedDate()),
                isRead,
                NotificationTypeGroup.isTypeInGroup(notification.getNotificationType().getNotificationTypeName(),
                        NOTICE),
                notification.getFeedId()
        );
    }

    static private String formatCreatedDate(LocalDateTime createdDate) {
        Duration duration = Duration.between(createdDate, LocalDateTime.now());

        long seconds = duration.getSeconds();
        if (seconds < 60) {
            return JUST_NOW;
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + MINUTE_UNIT;
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + HOUR_UNIT;
        }

        return createdDate.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    }
}
