package org.websoso.WSSServer.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.notification.repository.UserDeviceRepository;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    // 사용자 디바이스 식별자와 FCM Token 정보 저장
    public void registerFcmToken(Long userId, String deviceIdentifier, String fcmToken) {
        userDeviceRepository.upsertFcmToken(userId, deviceIdentifier, fcmToken);
    }

    // 사용자 디바이스 식별자와 FCM Token 정보 물리 삭제
    public void deleteDeviceIdentifier(User user, String deviceIdentifier) {
        userDeviceRepository.deleteByUserAndDeviceIdentifier(user, deviceIdentifier);
    }

}
