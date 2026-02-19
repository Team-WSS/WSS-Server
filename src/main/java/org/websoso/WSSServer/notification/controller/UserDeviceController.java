package org.websoso.WSSServer.notification.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.dto.user.FCMTokenRequest;
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
    public ResponseEntity<Void> registerFCMToken(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody FCMTokenRequest fcmTokenRequest) {
        return this.userDeviceService.registerFCMToken(user, fcmTokenRequest)
                ? ResponseEntity.status(CREATED).build()
                : ResponseEntity.status(NO_CONTENT).build();
    }

}
