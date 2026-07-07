package org.websoso.WSSServer.notification.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.websoso.WSSServer.feed.report.event.ReportMessageCreatedEvent;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessage;
import org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType;

@Component
@RequiredArgsConstructor
public class ReportMessageCreatedListener {

    private final DiscordMessageClient discordMessageClient;

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReportMessageCreatedEvent event) {
        discordMessageClient.sendDiscordWebhookMessage(
                DiscordWebhookMessage.of(event.content(), DiscordWebhookMessageType.REPORT)
        );
    }
}
