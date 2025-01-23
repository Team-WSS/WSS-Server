package org.websoso.WSSServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.domain.UserDevice;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

}
