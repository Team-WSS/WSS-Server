package org.websoso.WSSServer.domain.common;

public record Message(
        String content
) {
    public static Message of(String content) {
        return new Message(content);
    }
}
