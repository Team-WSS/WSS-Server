package org.websoso.WSSServer.domain.common;

public record Message(
        String content
) {
    public static Message of(String content) {
        if (content.length() >= 2000) {
            content = content.substring(0, 1993) + "\n...```";
        }
        return new Message(content);
    }

}
