package org.websoso.WSSServer.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.notification.domain.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationCustomRepository {

}
