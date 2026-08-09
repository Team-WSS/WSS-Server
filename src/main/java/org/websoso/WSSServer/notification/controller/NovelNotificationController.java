package org.websoso.WSSServer.notification.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.LAST_SUBSCRIPTION_ID_POSITIVE_OR_ZERO;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_ID_POSITIVE;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.SIZE_MAX;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.SIZE_MIN;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.notification.application.NovelNotificationApplication;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationDeleteRequest;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationUpdateRequest;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationPageResponse;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationResponse;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.user.domain.User;

/** 작품별 알림 설정과 설정 화면의 구독 목록 API를 제공한다. */
@RestController
@RequiredArgsConstructor
@Validated
public class NovelNotificationController {

    private final NovelNotificationApplication novelNotificationApplication;

    /** 작품에 등록된 사용자의 완결·휴재 복귀 알림 상태를 조회한다. */
    @GetMapping("/novels/{novelId}/notification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NovelNotificationResponse> getSettings(
            @AuthenticationPrincipal User user,
            @PathVariable @Positive(message = NOVEL_ID_POSITIVE) Long novelId
    ) {
        return ResponseEntity
                .status(OK)
                .body(novelNotificationApplication.getSettings(user, novelId));
    }

    /** 작품의 완결·휴재 복귀 알림 상태를 한 번에 갱신한다. */
    @PutMapping("/novels/{novelId}/notification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateSettings(
            @AuthenticationPrincipal User user,
            @PathVariable @Positive(message = NOVEL_ID_POSITIVE) Long novelId,
            @Valid @RequestBody NovelNotificationUpdateRequest request
    ) {
        novelNotificationApplication.updateSettings(user, novelId, request);

        return ResponseEntity.status(NO_CONTENT).build();
    }

    /** 설정 화면에서 알림 유형별 등록 작품을 조회한다. */
    @GetMapping("/users/me/notification/novels")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NovelNotificationPageResponse> getSubscriptions(
            @AuthenticationPrincipal User user,
            @RequestParam NovelNotificationType notificationType,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = LAST_SUBSCRIPTION_ID_POSITIVE_OR_ZERO) Long lastSubscriptionId,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = SIZE_MIN) @Max(value = 50, message = SIZE_MAX) int size
    ) {
        return ResponseEntity
                .status(OK)
                .body(novelNotificationApplication.getSubscriptions(user, notificationType, lastSubscriptionId, size));
    }

    /** 설정 화면에서 선택한 같은 유형의 작품 알림을 일괄 삭제한다. */
    @DeleteMapping("/users/me/notification/novels")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSubscriptions(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody NovelNotificationDeleteRequest request
    ) {
        novelNotificationApplication.deleteSubscriptions(user, request);

        return ResponseEntity.status(NO_CONTENT).build();
    }
}
