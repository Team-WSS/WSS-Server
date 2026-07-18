package org.websoso.WSSServer.notification.listener;

import static org.mockito.BDDMockito.then;
import static org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.REPORT;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.feed.report.event.ReportMessageCreatedEvent;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessage;

@ExtendWith(MockitoExtension.class)
class ReportMessageCreatedListenerTest {

    @InjectMocks
    private ReportMessageCreatedListener listener;

    @Mock
    private DiscordMessageClient discordMessageClient;

    @DisplayName("신고 메시지 생성 이벤트를 받으면 디스코드로 전송한다")
    @Test
    void sendsDiscordMessage() {
        ReportMessageCreatedEvent event = ReportMessageCreatedEvent.of("report message");

        listener.handle(event);

        then(discordMessageClient).should().sendDiscordWebhookMessage(
                DiscordWebhookMessage.of("report message", REPORT)
        );
    }
}
