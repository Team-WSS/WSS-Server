package org.websoso.WSSServer.notification.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.notification.repository.UserDeviceRepository;
import org.websoso.WSSServer.user.domain.User;

@ExtendWith(MockitoExtension.class)
class UserDeviceServiceTest {

    @InjectMocks
    private UserDeviceService userDeviceService;

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private User user;

    @Nested
    @DisplayName("registerFcmToken")
    class RegisterFcmToken {

        @DisplayName("FCM 토큰을 등록한다")
        @Test
        void registersFcmToken() {
            // given
            Long userId = 1L;
            String deviceIdentifier = "device-123";
            String fcmToken = "fcm-token-abc";

            // when
            userDeviceService.registerFcmToken(userId, deviceIdentifier, fcmToken);

            // then
            verify(userDeviceRepository).upsertFcmToken(userId, deviceIdentifier, fcmToken);
        }

        @DisplayName("동일 디바이스의 FCM 토큰을 업데이트한다")
        @Test
        void updatesExistingFcmToken() {
            // given
            Long userId = 1L;
            String deviceIdentifier = "device-123";
            String newFcmToken = "new-fcm-token-xyz";

            // when
            userDeviceService.registerFcmToken(userId, deviceIdentifier, newFcmToken);

            // then
            verify(userDeviceRepository).upsertFcmToken(userId, deviceIdentifier, newFcmToken);
        }
    }

    @Nested
    @DisplayName("deleteDeviceIdentifier")
    class DeleteDeviceIdentifier {

        @DisplayName("디바이스를 삭제한다")
        @Test
        void deletesDeviceIdentifier() {
            // given
            String deviceIdentifier = "device-123";

            // when
            userDeviceService.deleteDeviceIdentifier(user, deviceIdentifier);

            // then
            verify(userDeviceRepository).deleteByUserAndDeviceIdentifier(user, deviceIdentifier);
        }
    }
}
