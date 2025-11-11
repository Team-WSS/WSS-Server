package org.websoso.WSSServer.notification;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class FCMClient {

    private final FirebaseMessaging firebaseMessaging;

    public void sendPushMessage(String targetFCMToken, FCMMessageRequest fcmMessageRequest) {
        Message message = createMessage(targetFCMToken, fcmMessageRequest);

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("[FirebaseMessagingException] exception ", e);
            // TODO: discord로 알림 추가 혹은 후속 작업 논의 후 추가
        }
    }

    private Message createMessage(String targetFCMToken, FCMMessageRequest fcmMessageRequest) {
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(fcmMessageRequest.title())
                                .setBody(fcmMessageRequest.body())
                                .build())
                        .build())
                .build();

        return Message.builder()
                .setToken(targetFCMToken)
                .putData("title", fcmMessageRequest.title())
                .putData("body", fcmMessageRequest.body())
                .putData("feedId", fcmMessageRequest.feedId())
                .putData("view", fcmMessageRequest.view())
                .putData("notificationId", fcmMessageRequest.notificationId())
                .setApnsConfig(apnsConfig)
                .build();
    }

    public void sendMulticastPushMessage(List<String> targetFCMTokens, FCMMessageRequest fcmMessageRequest) {
        MulticastMessage multicastMessage = createMulticastMessage(targetFCMTokens, fcmMessageRequest);
        try {
            BatchResponse batchResponse = firebaseMessaging.sendEachForMulticast(multicastMessage);
            // 푸시알림 전송 실패한 메시지 로그 기록
            List<SendResponse> responses = batchResponse.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                if (responses.get(i).isSuccessful()) {
                    log.info("[FCM 전송 성공] Token: {}", targetFCMTokens.get(i));
                } else {
                    log.error("[FCM 전송 실패] Token: {} - Error: {}", targetFCMTokens.get(i),
                            responses.get(i).getException().getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[FirebaseMessagingException] exception ", e);
            // TODO: discord로 알림 추가 혹은 후속 작업 논의 후 추가
        }
    }

    private MulticastMessage createMulticastMessage(List<String> targetFCMTokens, FCMMessageRequest fcmMessageRequest) {
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(fcmMessageRequest.title())
                                .setBody(fcmMessageRequest.body())
                                .build())
                        .build())
                .build();

        return MulticastMessage.builder()
                .addAllTokens(targetFCMTokens)
                .putData("title", fcmMessageRequest.title())
                .putData("body", fcmMessageRequest.body())
                .putData("feedId", fcmMessageRequest.feedId())
                .putData("view", fcmMessageRequest.view())
                .putData("notificationId", fcmMessageRequest.notificationId())
                .setApnsConfig(apnsConfig)
                .build();
    }
}
