package org.websoso.WSSServer.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.notification.repository.UserDeviceRepository;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    @Transactional
    public void registerFcmToken(Long userId, String deviceIdentifier, String fcmToken) {
        userDeviceRepository.upsertFcmToken(userId, deviceIdentifier, fcmToken);
    }

    @Transactional
    public void deleteDeviceIdentifier(User user, String deviceIdentifier) {
        userDeviceRepository.deleteByUserAndDeviceIdentifier(user, deviceIdentifier);
    }

}
