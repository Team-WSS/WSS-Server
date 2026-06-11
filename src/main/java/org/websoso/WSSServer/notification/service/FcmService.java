package org.websoso.WSSServer.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.UserDevice;
import org.websoso.WSSServer.notification.dto.FCMMessageRequest;
import org.websoso.WSSServer.notification.infrastructure.FCMClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final FCMClient fcmClient;

    public void sendPushFeedNotification(Notification notification, List<UserDevice> devices) {

        FCMMessageRequest fcmMessageRequest = FCMMessageRequest.of(
                notification.getNotificationTitle(),
                notification.getNotificationBody(),
                String.valueOf(notification.getFeedId()),
                "feedDetail",
                String.valueOf(notification.getNotificationId())
        );

        List<String> targetFCMTokens = devices.stream()
                .map(UserDevice::getFcmToken)
                .toList();

        fcmClient.sendMulticastPushMessage(targetFCMTokens, fcmMessageRequest);
    }

}
