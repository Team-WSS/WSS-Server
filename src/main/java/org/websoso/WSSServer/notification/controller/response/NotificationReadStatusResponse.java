package org.websoso.WSSServer.notification.controller.response;

public record NotificationReadStatusResponse(
        boolean hasUnreadNotifications
) {
    public static NotificationReadStatusResponse of(boolean hasUnreadNotifications) {
        return new NotificationReadStatusResponse(hasUnreadNotifications);
    }
}
