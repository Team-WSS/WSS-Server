package org.websoso.WSSServer.service;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.websoso.WSSServer.domain.common.Message;

@Service
@Slf4j
public class MessageService {

    @Value("${logging.discord.webhook-url}")
    String discordWebhookUrl;

    public void sendMessage(Message message) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");
            HttpEntity<Message> messageEntity = new HttpEntity<>(message, httpHeaders);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> response = template.exchange(
                    discordWebhookUrl,
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
