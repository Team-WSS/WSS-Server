package org.websoso.WSSServer.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.websoso.WSSServer.application.NotificationApplication;
import org.websoso.WSSServer.notification.controller.response.NotificationDetailResponse;
import org.websoso.WSSServer.notification.controller.response.NotificationPageResponse;
import org.websoso.WSSServer.notification.controller.response.NotificationReadStatusResponse;
import org.websoso.WSSServer.user.domain.User;

/** 알림 Controller의 요청 위임과 상태 코드를 검증한다. */
class NotificationControllerTest {

    private final NotificationApplication notificationApplication = mock(NotificationApplication.class);
    private final NotificationController controller = new NotificationController(notificationApplication);
    private final User user = mock(User.class);

    @DisplayName("알림 목록 조회 결과와 200을 반환한다")
    @Test
    void getsNotifications() {
        NotificationPageResponse expected = new NotificationPageResponse(false, List.of());
        given(notificationApplication.getNotifications(user, 20L, 15)).willReturn(expected);

        ResponseEntity<NotificationPageResponse> response = controller.getNotifications(user, 20L, 15);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        then(notificationApplication).should().getNotifications(user, 20L, 15);
    }

    @DisplayName("공지 알림 상세 조회 결과와 200을 반환한다")
    @Test
    void getsNotificationDetail() {
        NotificationDetailResponse expected =
                new NotificationDetailResponse("서비스 점검 안내", "2026.08.22", "점검 상세 내용");
        given(notificationApplication.getNotificationDetail(user, 10L)).willReturn(expected);

        ResponseEntity<NotificationDetailResponse> response = controller.getNotification(user, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        then(notificationApplication).should().getNotificationDetail(user, 10L);
    }

    @DisplayName("읽지 않은 알림 상태 조회 결과와 200을 반환한다")
    @Test
    void getsNotificationStatus() {
        NotificationReadStatusResponse expected = new NotificationReadStatusResponse(true);
        given(notificationApplication.getReadStatus(user)).willReturn(expected);

        ResponseEntity<NotificationReadStatusResponse> response = controller.getNotificationStatus(user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        then(notificationApplication).should().getReadStatus(user);
    }

    @DisplayName("알림을 읽음 처리하고 204를 반환한다")
    @Test
    void updatesNotificationReadStatus() {
        ResponseEntity<Void> response = controller.createNotificationAsReadStatus(user, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        then(notificationApplication).should().updateNotificationReadStatus(user, 10L);
    }

    @SuppressWarnings("deprecation")
    @DisplayName("기존 읽지 않은 알림 상태 조회 결과와 200을 반환한다")
    @Test
    void getsNotificationStatusDeprecated() {
        NotificationReadStatusResponse expected = new NotificationReadStatusResponse(true);
        given(notificationApplication.getReadStatus(user)).willReturn(expected);

        ResponseEntity<NotificationReadStatusResponse> response =
                controller.checkNotificationsReadStatusDeprecated(user);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        then(notificationApplication).should().getReadStatus(user);
    }

    @SuppressWarnings("deprecation")
    @DisplayName("기존 알림 읽음 처리는 201을 반환한다")
    @Test
    void createsNotificationAsReadDeprecated() {
        ResponseEntity<Void> response = controller.createNotificationAsRead(user, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNull();
        then(notificationApplication).should().updateNotificationReadStatus(user, 10L);
    }

}
