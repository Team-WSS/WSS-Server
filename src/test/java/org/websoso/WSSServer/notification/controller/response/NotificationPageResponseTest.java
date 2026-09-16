package org.websoso.WSSServer.notification.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.NotificationType;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;

/** 앱 내 알림 목록이 작품 이동 정보를 포함하는지 검증한다. */
class NotificationPageResponseTest {

    /** 작품 알림의 novelId를 기존 목록 응답에 포함하는지 검증한다. */
    @Test
    @DisplayName("작품 알림의 작품 ID를 반환한다")
    void includesNovelId() {
        NotificationType notificationType = mock(NotificationType.class);
        given(notificationType.getNotificationTypeName()).willReturn("완결 알림");
        given(notificationType.getNotificationTypeImage()).willReturn("image");
        Notification notification = Notification.createNoticeNotification(
                "완결 알림",
                "작품이 완결났어요.",
                null,
                1L,
                notificationType
        );
        ReflectionTestUtils.setField(notification, "notificationId", 10L);
        ReflectionTestUtils.setField(notification, "novelId", 100L);
        ReflectionTestUtils.setField(notification, "createdDate", LocalDateTime.now());

        NotificationPageResponse response = NotificationPageResponse.from(
                new SliceImpl<>(List.of(new ReadNotificationDto(notification, false)))
        );

        assertThat(response.notifications()).singleElement()
                .extracting("feedId", "novelId", "isNotice")
                .containsExactly(null, 100L, false);
    }
}
