package org.websoso.WSSServer.notification.dto;

public record NotificationsReadStatusGetResponse(
        Boolean hasUnreadNotifications
) {
    static public NotificationsReadStatusGetResponse of(Boolean hasUnreadNotifications) {
        return new NotificationsReadStatusGetResponse(hasUnreadNotifications);
    }
}
