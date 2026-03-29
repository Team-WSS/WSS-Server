package org.websoso.WSSServer.notification.controller.response;

import static org.websoso.WSSServer.domain.common.NotificationTypeGroup.NOTICE;

import java.util.List;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.domain.common.NotificationTypeGroup;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;
import org.websoso.WSSServer.util.TimeFormatUtil;

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

        public static NotificationItem from(ReadNotificationDto dto) {
            Notification notification = dto.notification();

            return new NotificationItem(
                    notification.getNotificationId(),
                    notification.getNotificationType().getNotificationTypeImage(),
                    notification.getNotificationTitle(),
                    notification.getNotificationBody(),
                    TimeFormatUtil.formatNotificationDate(notification.getCreatedDate()),
                    dto.isRead(),
                    NotificationTypeGroup.isTypeInGroup(notification.getNotificationType().getNotificationTypeName(), NOTICE),
                    notification.getFeedId()
            );
        }

    }
}