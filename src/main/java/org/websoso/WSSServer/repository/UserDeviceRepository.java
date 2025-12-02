package org.websoso.WSSServer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.domain.UserDevice;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    Optional<UserDevice> findByDeviceIdentifierAndUser(String deviceIdentifier, User user);

    void deleteByUserAndDeviceIdentifier(User user, String deviceIdentifier);
}
