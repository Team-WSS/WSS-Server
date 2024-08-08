package org.websoso.WSSServer.domain.common;

public record DiscordWebhookMessage(
        String content
) {
    public static DiscordWebhookMessage of(String content) {
        if (content.length() >= 2000) {
            content = content.substring(0, 1993) + "\n...```";
        }
        return new DiscordWebhookMessage(content);
    }

}
