package org.websoso.WSSServer.notification.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static software.amazon.awssdk.http.HttpStatusCode.NO_CONTENT;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.notification.dto.FCMTokenRequest;
import org.websoso.WSSServer.notification.service.UserDeviceService;
import org.websoso.WSSServer.user.domain.User;

@RestController
@Validated
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    // 사용자의 FCM 토큰을 저장한다.
    @PostMapping("/users/fcm-token")
    @PreAuthorize("isAuthenticated()")
    @Deprecated(since = "/user/me/devices로 이전되면 삭제될 예정")
    public ResponseEntity<Void> registerFCMToken(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody FCMTokenRequest fcmTokenRequest) {
        this.userDeviceService.registerFcmToken(user.getUserId(), fcmTokenRequest.deviceIdentifier(),
                fcmTokenRequest.fcmToken());

        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/users/me/devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> registerFcmToken(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FCMTokenRequest fcmTokenRequest
    ) {
        this.userDeviceService.registerFcmToken(
                user.getUserId(), fcmTokenRequest.deviceIdentifier(), fcmTokenRequest.fcmToken());

        return ResponseEntity.status(NO_CONTENT).build();
    }

}
