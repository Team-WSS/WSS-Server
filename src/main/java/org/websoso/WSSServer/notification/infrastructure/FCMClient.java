package org.websoso.WSSServer.notification.infrastructure;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import java.util.ArrayList;
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

    public List<String> sendPushMessage(String targetFCMToken, FCMMessageRequest fcmMessageRequest) {
        List<String> failedTokens = new ArrayList<>();
        Message message = createMessage(targetFCMToken, fcmMessageRequest);

        try {
            firebaseMessaging.send(message);
            log.debug("[FCM] Push message sent successfully to token: {}", maskToken(targetFCMToken));
        } catch (FirebaseMessagingException e) {
            handleFirebaseException(e, targetFCMToken, failedTokens);
        }
        return failedTokens;
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

    public List<String> sendMulticastPushMessage(List<String> targetFCMTokens, FCMMessageRequest fcmMessageRequest) {
        List<String> failedTokens = new ArrayList<>();
        MulticastMessage multicastMessage = createMulticastMessage(targetFCMTokens, fcmMessageRequest);

        try {
            BatchResponse batchResponse = firebaseMessaging.sendEachForMulticast(multicastMessage);
            List<SendResponse> responses = batchResponse.getResponses();
            int successCount = 0;

            for (int i = 0; i < responses.size(); i++) {
                SendResponse response = responses.get(i);
                String token = targetFCMTokens.get(i);

                if (response.isSuccessful()) {
                    successCount++;
                } else {
                    FirebaseMessagingException exception = response.getException();
                    if (exception != null && isUnregisteredException(exception)) {
                        log.warn("[FCM] Token unregistered/expired: {}, reason: {}", maskToken(token), exception.getMessage());
                        failedTokens.add(token);
                    } else {
                        log.error("[FCM] Send failed for token: {}, error: {}", maskToken(token),
                                exception != null ? exception.getMessage() : "unknown");
                    }
                }
            }
            log.info("[FCM] Multicast result: success={}, failed={}, total={}", successCount, failedTokens.size(), targetFCMTokens.size());
        } catch (FirebaseMessagingException e) {
            log.error("[FCM] Multicast send failed completely: {}", e.getMessage(), e);
            failedTokens.addAll(targetFCMTokens);
        }
        return failedTokens;
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

    private void handleFirebaseException(FirebaseMessagingException e, String token, List<String> failedTokens) {
        if (isUnregisteredException(e)) {
            log.warn("[FCM] Token unregistered/expired: {}, reason: {}", maskToken(token), e.getMessage());
            failedTokens.add(token);
        } else {
            log.error("[FCM] Firebase messaging error for token: {}, errorCode={}, message={}",
                    maskToken(token), e.getErrorCode(), e.getMessage(), e);
        }
    }

    private boolean isUnregisteredException(FirebaseMessagingException e) {
        return e.getErrorCode() == ErrorCode.INVALID_ARGUMENT;
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
    }
}
