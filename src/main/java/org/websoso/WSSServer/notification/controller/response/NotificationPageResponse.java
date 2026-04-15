package org.websoso.WSSServer.notification.controller.response;

import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;

public record NotificationPageResponse(
        boolean isLoadable,
        List<NotificationItem> notifications
) {
    public static NotificationPageResponse from(Slice<ReadNotificationDto> notificationSlice) {
        List<NotificationItem> items = notificationSlice.getContent().stream()
                .map(NotificationItem::from)
                .toList();

        return new NotificationPageResponse(
                notificationSlice.hasNext(),
                items
        );
    }

    public record NotificationItem(
            Long notificationId,
            String notificationImage,
            String notificationTitle,
            String notificationBody,
            String createdDate,
            boolean isRead,
            boolean isNotice,
            Long feedId
    ) {
        private static final String JUST_NOW = "방금 전";
        private static final String MINUTE_UNIT = "분 전";
        private static final String HOUR_UNIT = "시간 전";
        private static final String DATE_PATTERN = "yyyy.MM.dd";

        public static NotificationItem from(ReadNotificationDto dto) {
            Notification notification = dto.notification();

            return new NotificationItem(
                    notification.getNotificationId(),
                    notification.getNotificationType().getNotificationTypeImage(),
                    notification.getNotificationTitle(),
                    notification.getNotificationBody(),
                    formatCreatedDate(notification.getCreatedDate()),
                    dto.isRead(),
                    NotificationTypeGroup.isTypeInGroup(notification.getNotificationType().getNotificationTypeName(), NOTICE),
                    notification.getFeedId()
            );
        }

        private static String formatCreatedDate(LocalDateTime createdDate) {
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
}