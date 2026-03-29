package org.websoso.WSSServer.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.ReadNotification;
import org.websoso.WSSServer.user.domain.User;

@Repository
public interface ReadNotificationRepository extends JpaRepository<ReadNotification, Long> {

    boolean existsByUserAndNotification(User user, Notification notification);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO read_notification (notification_id, user_id)
            VALUES (:notificationId, :userId)
            """, nativeQuery = true)
    void insertIgnoreReadNotification(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId
    );
}
