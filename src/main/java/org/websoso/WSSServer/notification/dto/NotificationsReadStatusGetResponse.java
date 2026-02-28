package org.websoso.WSSServer.notification.dto;

public record NotificationsReadStatusGetResponse(
        boolean hasUnreadNotifications
) {
    public static NotificationsReadStatusGetResponse of(boolean hasUnreadNotifications) {
        return new NotificationsReadStatusGetResponse(hasUnreadNotifications);
    }
}
