package org.websoso.WSSServer.notification.listener;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.feed.report.event.ReportMessageCreatedEvent;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;

@ExtendWith(MockitoExtension.class)
class ReportMessageCreatedListenerTest {

    @InjectMocks
    private ReportMessageCreatedListener listener;

    @Mock
    private DiscordMessageClient discordMessageClient;

    @DisplayName("메시지 생성 이벤트를 받으면 디스코드로 전송한다")
    @Test
    void sendsDiscordMessage() {
        // given
        ReportMessageCreatedEvent event = ReportMessageCreatedEvent.of("report message");

        // when
        listener.handle(event);

        // then
        then(discordMessageClient).should().sendDiscordWebhookMessage(
                org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessage.of(
                        "report message",
                        org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.REPORT
                )
        );
    }
}
