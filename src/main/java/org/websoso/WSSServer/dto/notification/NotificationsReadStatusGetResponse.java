package org.websoso.WSSServer.dto.notification;

public record NotificationsReadStatusGetResponse(
        Boolean hasUnreadNotifications
) {
    static public NotificationsReadStatusGetResponse of(Boolean hasUnreadNotifications) {
        return new NotificationsReadStatusGetResponse(hasUnreadNotifications);
    }
}
