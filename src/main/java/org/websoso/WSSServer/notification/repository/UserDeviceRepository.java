package org.websoso.WSSServer.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.notification.domain.UserDevice;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO user_device (user_id, device_identifier, fcm_token)
            VALUES (:userId, :deviceIdentifier, :fcmToken)
            ON DUPLICATE KEY UPDATE fcm_token = :fcmToken
            """, nativeQuery = true)
    void upsertFcmToken(@Param("userId") Long userId,
                        @Param("deviceIdentifier") String deviceIdentifier,
                        @Param("fcmToken") String fcmToken);

    void deleteByUserAndDeviceIdentifier(User user, String deviceIdentifier);
}
