package org.websoso.WSSServer.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.websoso.WSSServer.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "SELECT n FROM Notification n WHERE "
            + "(:lastNotificationId = 0 OR n.notificationId < :lastNotificationId) "
            + "AND (n.userId = 0 OR n.userId = :userId) "
            + "ORDER BY n.createdDate DESC")
    Slice<Notification> findNotifications(Long lastNotificationId, Long userId, PageRequest pageRequest);
}
