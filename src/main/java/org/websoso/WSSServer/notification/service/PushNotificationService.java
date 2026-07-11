package org.websoso.WSSServer.notification.service;

import static java.lang.Boolean.TRUE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.UserDevice;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final UserService userService;
    private final UserDeviceService userDeviceService;
    private final FcmService fcmService;

    public void sendIfEnabled(Long userId, Notification notification) {
        User target = userService.getUserOrException(userId);

        if (!TRUE.equals(target.getIsPushEnabled())) {
            return;
        }

        List<UserDevice> devices = userDeviceService.findUserDevices(target.getUserId());
        if (devices.isEmpty()) {
            return;
        }

        fcmService.sendPushFeedNotification(notification, devices);
    }
}
