package org.websoso.WSSServer.notification;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendPushMessage(String targetFCMToken, String title, String body, String clickAction) {
        try {
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setNotification(AndroidNotification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setClickAction(clickAction)
                            .build()
                    )
                    .build();

            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setCategory(clickAction)
                            .build())
                    .build();

            Message message = Message.builder()
                    .setToken(targetFCMToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .build();

            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("[FirebaseMessagingException] exception ", e);
        }
    }
}
