package org.websoso.WSSServer.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.websoso.WSSServer.notification.application.NovelNotificationApplication;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationDeleteRequest;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationUpdateRequest;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.user.domain.User;

/** 작품 알림 설정 Controller의 요청 위임과 상태 코드를 검증한다. */
class NovelNotificationControllerTest {

    private final NovelNotificationApplication novelNotificationApplication =
            mock(NovelNotificationApplication.class);
    private final NovelNotificationController controller =
            new NovelNotificationController(novelNotificationApplication);
    private final User user = mock(User.class);

    /** 두 토글 갱신 요청을 위임하고 204를 반환하는지 검증한다. */
    @Test
    @DisplayName("작품 알림 설정을 갱신하면 204를 반환한다")
    void updatesSettings() {
        NovelNotificationUpdateRequest request = new NovelNotificationUpdateRequest(true, false);

        ResponseEntity<Void> response = controller.updateSettings(user, 10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(novelNotificationApplication).updateSettings(user, 10L, request);
    }

    /** 일괄 삭제 요청을 위임하고 204를 반환하는지 검증한다. */
    @Test
    @DisplayName("선택한 작품 알림을 삭제하면 204를 반환한다")
    void deletesSubscriptions() {
        NovelNotificationDeleteRequest request = new NovelNotificationDeleteRequest(
                NovelNotificationType.COMPLETION,
                List.of(10L, 20L)
        );

        ResponseEntity<Void> response = controller.deleteSubscriptions(user, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(novelNotificationApplication).deleteSubscriptions(user, request);
    }
}
