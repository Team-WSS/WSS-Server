package org.websoso.WSSServer.notification;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendPushMessage(String targetFCMToken, String title, String body, String clickAction) {
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setNotification(AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setClickAction(clickAction)
                        .build()
                )
                .build();
    }
}
