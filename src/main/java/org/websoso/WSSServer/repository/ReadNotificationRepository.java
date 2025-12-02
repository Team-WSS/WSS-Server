package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.Notification;
import org.websoso.WSSServer.domain.ReadNotification;
import org.websoso.WSSServer.user.domain.User;

@Repository
public interface ReadNotificationRepository extends JpaRepository<ReadNotification, Long> {

    List<ReadNotification> findAllByUser(User user);

    boolean existsByUserAndNotification(User user, Notification notification);
}
