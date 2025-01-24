package org.websoso.WSSServer.notification.dto;

public record FCMMessageRequest(
        String title,
        String body,
        String feedId,
        String view,
        String notificationId
) {

    public static FCMMessageRequest of(String title, String body, String feedId, String view, String notificationId) {
        return new FCMMessageRequest(title, body, feedId, view, notificationId);
    }
}
