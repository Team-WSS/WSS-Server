package org.websoso.WSSServer.notification.dto;

import org.websoso.WSSServer.notification.domain.Notification;

public record ReadNotificationDto(
        Notification notification,
        boolean isRead
) {
}
