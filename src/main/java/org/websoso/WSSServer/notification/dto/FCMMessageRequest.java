package org.websoso.WSSServer.notification.dto;

public record FCMMessageRequest(
        String title,
        String body,
        String feedId,
        String view
) {

    public static FCMMessageRequest of(String title, String body, String feedId, String view) {
        return new FCMMessageRequest(title, body, feedId, view);
    }
}
