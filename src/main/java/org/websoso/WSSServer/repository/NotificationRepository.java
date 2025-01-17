package org.websoso.WSSServer.repository;

import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = "SELECT n FROM Notification n WHERE "
            + "(:lastNotificationId = 0 OR n.notificationId < :lastNotificationId) "
            + "AND (n.userId = 0 OR n.userId = :userId) "
            + "ORDER BY n.createdDate DESC")
    Slice<Notification> findNotifications(Long lastNotificationId, Long userId, PageRequest pageRequest);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Notification n " +
            "WHERE n.userId IN :userIds " +
            "AND n.notificationId NOT IN (" +
            "SELECT rn.notification.notificationId FROM ReadNotification rn WHERE rn.user = :user)")
    boolean existsUnreadNotifications(Set<Long> userIds, User user);
}
