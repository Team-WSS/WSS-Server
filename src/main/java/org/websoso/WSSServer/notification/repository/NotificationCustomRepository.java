package org.websoso.WSSServer.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;

public interface NotificationCustomRepository {

    boolean existsUnreadNotifications(Long userId);

    Slice<ReadNotificationDto> findNotifications(Long lastNotificationId, Long userId, Pageable pageable);
}
