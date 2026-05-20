package org.websoso.WSSServer.infrastructure.discord;

import static org.springframework.http.HttpMethod.POST;
import static org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.JOIN;
import static org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.WITHDRAW;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordMessageClient {

    private final RestTemplate restTemplate;

    @Value("${logging.discord.report-webhook-url}")
    private String discordReportWebhookUrl;
    @Value("${logging.discord.withdraw-webhook-url}")
    private String discordWithdrawWebhookUrl;
    @Value("${logging.discord.join-webhook-url}")
    private String discordJoinWebhookUrl;

    public void sendDiscordWebhookMessage(DiscordWebhookMessage message) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; utf-8");
        HttpEntity<DiscordWebhookMessage> messageEntity = new HttpEntity<>(message, httpHeaders);
        String webhookUrl = resolveWebhookUrl(message);

        try {
            restTemplate.exchange(webhookUrl, POST, messageEntity, String.class);
            log.debug("Discord webhook message sent successfully: type={}", message.type());
        } catch (RestClientException e) {
            log.error("Failed to send Discord webhook message: type={}, error={}", message.type(), e.getMessage(), e);
        }
    }

    private String resolveWebhookUrl(DiscordWebhookMessage message) {
        if (message.type() == REPORT) {
            return discordReportWebhookUrl;
        } else if (message.type() == WITHDRAW) {
            return discordWithdrawWebhookUrl;
        } else if (message.type() == JOIN) {
            return discordJoinWebhookUrl;
        }
        throw new IllegalArgumentException("Unknown Discord webhook message type: " + message.type());
    }
}
