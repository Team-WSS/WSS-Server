package org.websoso.WSSServer.notification.service;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.JOIN;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.REPORT;
import static org.websoso.WSSServer.domain.common.DiscordWebhookMessageType.WITHDRAW;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.websoso.WSSServer.domain.common.DiscordWebhookMessage;

@Service
@Slf4j
public class DiscordMessageClient {

    @Value("${logging.discord.report-webhook-url}")
    private String discordReportWebhookUrl;
    @Value("${logging.discord.withdraw-webhook-url}")
    private String discordWithdrawWebhookUrl;
    @Value("${logging.discord.join-webhook-url}")
    private String discordJoinWebhookUrl;

    public void sendDiscordWebhookMessage(DiscordWebhookMessage message) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");
            HttpEntity<DiscordWebhookMessage> messageEntity = new HttpEntity<>(message, httpHeaders);
            String webhookUrl = "";

            if (message.type() == REPORT) {
                webhookUrl = discordReportWebhookUrl;
            } else if (message.type() == WITHDRAW) {
                webhookUrl = discordWithdrawWebhookUrl;
            } else if (message.type() == JOIN) {
                webhookUrl = discordJoinWebhookUrl;
            }

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> response = template.exchange(
                    webhookUrl,
                    POST,
                    messageEntity,
                    String.class
            );

            if (response.getStatusCode().value() != NO_CONTENT.value()) {
                log.error("메시지 전송 이후 에러 발생");
            }

        } catch (Exception e) {
            log.error("에러 발생 :: " + e);
        }
    }
}
