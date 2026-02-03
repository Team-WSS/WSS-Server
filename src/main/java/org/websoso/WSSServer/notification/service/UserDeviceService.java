package org.websoso.WSSServer.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.websoso.WSSServer.dto.user.FCMTokenRequest;
import org.websoso.WSSServer.notification.repository.UserDeviceRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.domain.UserDevice;

@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;

    // 사용자 디바이스 식별자와 FCM Token 정보 저장
    public boolean registerFCMToken(User user, FCMTokenRequest fcmTokenRequest) {
        return userDeviceRepository.findByDeviceIdentifierAndUser(fcmTokenRequest.deviceIdentifier(), user)
                .map(userDevice -> {
                    userDevice.updateFcmToken(fcmTokenRequest.fcmToken());
                    return false;
                })
                .orElseGet(() -> {
                    UserDevice userDevice = UserDevice.create(
                            fcmTokenRequest.fcmToken(),
                            fcmTokenRequest.deviceIdentifier(),
                            user
                    );
                    userDeviceRepository.save(userDevice);
                    return true;
                });
    }

    // 사용자 디바이스 식별자와 FCM Token 정보 물리 삭제
    public void deleteDeviceIdentifier(User user, String deviceIdentifier) {
        userDeviceRepository.deleteByUserAndDeviceIdentifier(user, deviceIdentifier);
    }

}
