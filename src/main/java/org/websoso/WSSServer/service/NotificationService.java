package org.websoso.WSSServer.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.ReadNotification;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.notification.NotificationInfo;
import org.websoso.WSSServer.dto.notification.NotificationsGetResponse;
import org.websoso.WSSServer.repository.NotificationRepository;
import org.websoso.WSSServer.repository.ReadNotificationRepository;

@Service
@AllArgsConstructor
@Transactional
public class NotificationService {

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private final NotificationRepository notificationRepository;
    private final ReadNotificationRepository readNotificationRepository;

    @Transactional(readOnly = true)
    public NotificationsGetResponse getNotifications(Long lastNotificationId, int size, User user) {
        Slice<Notification> notifications = notificationRepository.findNotifications(lastNotificationId,
                user.getUserId(), PageRequest.of(DEFAULT_PAGE_NUMBER, size));

        Set<Notification> readNotifications = readNotificationRepository.findAllByUser(user).stream()
                .map(ReadNotification::getNotification)
                .collect(Collectors.toSet());

        List<NotificationInfo> notificationInfos = notifications.getContent().stream()
                .map(n -> NotificationInfo.of(n, readNotifications.contains(n)))
                .toList();

        return NotificationsGetResponse.of(notifications.hasNext(), notificationInfos);
    }
}
