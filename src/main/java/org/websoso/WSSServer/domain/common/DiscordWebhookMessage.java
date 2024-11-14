package org.websoso.WSSServer.domain.common;

public record DiscordWebhookMessage(
        String content,
        String type
) {
    
    public static DiscordWebhookMessage of(String content, String type) {
        if (content.length() >= 2000) {
            content = content.substring(0, 1993) + "\n...```";
        }
        return new DiscordWebhookMessage(content, type);
    }
}
