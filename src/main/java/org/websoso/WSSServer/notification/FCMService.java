package org.websoso.WSSServer.notification;

import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendPushMessage(String targetFCMToken, String title, String body, String clickAction) {
        Message message = createMessage(targetFCMToken, title, body, clickAction);
    public void sendPushMessage(String targetFCMToken, String title, String body, String feedId, String view) {
        Message message = createMessage(targetFCMToken, title, body, feedId, view);

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("[FirebaseMessagingException] exception ", e);
            // TODO: discord로 알림 추가 혹은 후속 작업 논의 후 추가
        }
    }

    private Message createMessage(String targetFCMToken, String title, String body, String feedId, String view) {
        ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setAlert(ApsAlert.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build())
                .build();

        return Message.builder()
                .setToken(targetFCMToken)
                .putData("title", title)
                .putData("body", body)
                .putData("feedId", feedId)
                .putData("view", view)
                .setApnsConfig(apnsConfig)
                .build();
    }
}
