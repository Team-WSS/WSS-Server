package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.NotificationType;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {

    NotificationType findByNotificationTypeName(String notificationTypeName);

    Optional<NotificationType> findOptionalByNotificationTypeName(String notificationTypeName);
}
