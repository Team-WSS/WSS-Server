package org.websoso.WSSServer.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;
import org.websoso.WSSServer.notification.domain.Notification;
import org.websoso.WSSServer.notification.domain.NotificationType;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;
import org.websoso.WSSServer.notification.repository.NotificationRepository;
import org.websoso.WSSServer.notification.repository.ReadNotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ReadNotificationRepository readNotificationRepository;

    @Nested
    @DisplayName("hasUnreadNotifications")
    class HasUnreadNotifications {

        @DisplayName("읽지 않은 알림이 존재하면 true를 반환한다")
        @Test
        void returnsTrueWhenUnreadNotificationsExist() {
            // given
            Long userId = 1L;
            given(notificationRepository.existsUnreadNotifications(userId)).willReturn(true);

            // when
            boolean result = notificationService.hasUnreadNotifications(userId);

            // then
            assertThat(result).isTrue();
        }

        @DisplayName("읽지 않은 알림이 존재하지 않으면 false를 반환한다")
        @Test
        void returnsFalseWhenNoUnreadNotifications() {
            // given
            Long userId = 1L;
            given(notificationRepository.existsUnreadNotifications(userId)).willReturn(false);

            // when
            boolean result = notificationService.hasUnreadNotifications(userId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getNotification")
    class GetNotification {

        @DisplayName("자신에게 온 알림을 조회할 수 있다")
        @Test
        void returnsNotificationWhenUserIsRecipient() {
            // given
            Long userId = 1L;
            Long notificationId = 100L;
            Notification notification = createNotification(notificationId, userId, "공지사항");
            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

            // when
            Notification result = notificationService.getNotification(userId, notificationId);

            // then
            assertThat(result.getNotificationId()).isEqualTo(notificationId);
        }

        @DisplayName("전체 공지(notificationId=0) 알림을 조회할 수 있다")
        @Test
        void returnsGlobalNotificationForAnyUser() {
            // given
            Long userId = 1L;
            Long notificationId = 100L;
            Notification notification = createNotification(notificationId, 0L, "전체 공지");
            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

            // when
            Notification result = notificationService.getNotification(userId, notificationId);

            // then
            assertThat(result.getUserId()).isEqualTo(0L);
        }

        @DisplayName("자신에게 온 알림이 아니면 예외가 발생한다")
        @Test
        void throwsExceptionWhenUserIsNotRecipient() {
            // given
            Long userId = 1L;
            Long notificationId = 100L;
            Notification notification = createNotification(notificationId, 2L, "다른 사용자 알림");
            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

            // when & then
            assertThatThrownBy(() -> notificationService.getNotification(userId, notificationId))
                    .isInstanceOf(CustomNotificationException.class);
        }

        @DisplayName("존재하지 않는 알림을 조회하면 예외가 발생한다")
        @Test
        void throwsExceptionWhenNotificationNotFound() {
            // given
            Long userId = 1L;
            Long notificationId = 999L;
            given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> notificationService.getNotification(userId, notificationId))
                    .isInstanceOf(CustomNotificationException.class);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @DisplayName("알림을 읽음 처리한다")
        @Test
        void marksNotificationAsRead() {
            // given
            Long userId = 1L;
            Long notificationId = 100L;

            // when
            notificationService.markAsRead(userId, notificationId);

            // then
            verify(readNotificationRepository).insertIgnoreReadNotification(notificationId, userId);
        }
    }

    @Nested
    @DisplayName("getNotifications")
    class GetNotifications {

        @DisplayName("알림 목록을 조회한다")
        @Test
        void returnsNotificationPage() {
            // given
            Long userId = 1L;
            Long lastNotificationId = null;
            int size = 10;
            PageRequest pageRequest = PageRequest.of(0, size);

            Notification notification = createNotification(100L, userId, "테스트 알림");
            ReadNotificationDto dto = new ReadNotificationDto(notification, false);
            Slice<ReadNotificationDto> slice = new SliceImpl<>(java.util.List.of(dto));

            given(notificationRepository.findNotifications(lastNotificationId, userId, pageRequest))
                    .willReturn(slice);

            // when
            var result = notificationService.getNotifications(userId, lastNotificationId, size);

            // then
            assertThat(result.notifications()).hasSize(1);
            assertThat(result.isLoadable()).isFalse();
        }
    }

    private Notification createNotification(Long notificationId, Long userId, String title) {
        NotificationType notificationType = mock(NotificationType.class);
        lenient().when(notificationType.getNotificationTypeImage()).thenReturn("");
        lenient().when(notificationType.getNotificationTypeName()).thenReturn("공지사항");
        Notification notification = Notification.createNoticeNotification(title, "body", null, userId, notificationType);
        ReflectionTestUtils.setField(notification, "notificationId", notificationId);
        ReflectionTestUtils.setField(notification, "createdDate", LocalDateTime.now());
        return notification;
    }
}
