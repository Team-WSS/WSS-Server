package org.websoso.WSSServer.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.notification.domain.NovelNotificationSubscription;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.notification.repository.NovelNotificationSubscriptionRepository;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

/** 작품 알림 구독의 등록·조회·삭제 서비스 동작을 검증한다. */
@ExtendWith(MockitoExtension.class)
class NovelNotificationServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long NOVEL_ID = 10L;

    @InjectMocks
    private NovelNotificationService novelNotificationService;

    @Mock
    private NovelNotificationSubscriptionRepository subscriptionRepository;

    @Nested
    @DisplayName("작품 알림 설정 갱신")
    class UpdateSettings {

        /** 등록되지 않은 유형만 새로 저장하는지 검증한다. */
        @Test
        @DisplayName("켠 유형이 등록되어 있지 않으면 새로 저장한다")
        void savesEnabledTypeWhenAbsent() {
            User user = createUser();
            Novel novel = createNovel();
            given(subscriptionRepository.findAllByUser_UserIdAndNovel_NovelId(USER_ID, NOVEL_ID))
                    .willReturn(List.of());

            novelNotificationService.updateSettings(user, novel, true, false);

            ArgumentCaptor<NovelNotificationSubscription> captor =
                    ArgumentCaptor.forClass(NovelNotificationSubscription.class);
            verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getNotificationType()).isEqualTo(NovelNotificationType.COMPLETION);
            verify(subscriptionRepository, never()).delete(any(NovelNotificationSubscription.class));
        }

        /** 끈 유형만 삭제하고 유지할 유형은 건드리지 않는지 검증한다. */
        @Test
        @DisplayName("끈 유형이 등록되어 있으면 삭제한다")
        void deletesDisabledTypeWhenPresent() {
            User user = createUser();
            Novel novel = createNovel();
            NovelNotificationSubscription completion = createSubscription(NovelNotificationType.COMPLETION);
            NovelNotificationSubscription hiatusReturn = createSubscription(NovelNotificationType.HIATUS_RETURN);
            given(subscriptionRepository.findAllByUser_UserIdAndNovel_NovelId(USER_ID, NOVEL_ID))
                    .willReturn(List.of(completion, hiatusReturn));

            novelNotificationService.updateSettings(user, novel, true, false);

            verify(subscriptionRepository).delete(hiatusReturn);
            verify(subscriptionRepository, never()).delete(completion);
            verify(subscriptionRepository, never()).save(any(NovelNotificationSubscription.class));
        }

        /** 발송이 끝난 구독을 다시 켜면 지우고 새로 등록해 등록일이 갱신되는지 검증한다. */
        @Test
        @DisplayName("발송이 끝난 구독을 다시 켜면 지우고 새로 등록한다")
        void reregistersSentSubscriptionWhenEnabledAgain() {
            User user = createUser();
            Novel novel = createNovel();
            NovelNotificationSubscription sent = createSentSubscription(NovelNotificationType.COMPLETION);
            given(subscriptionRepository.findAllByUser_UserIdAndNovel_NovelId(USER_ID, NOVEL_ID))
                    .willReturn(List.of(sent));

            novelNotificationService.updateSettings(user, novel, true, false);

            InOrder inOrder = inOrder(subscriptionRepository);
            inOrder.verify(subscriptionRepository).delete(sent);
            inOrder.verify(subscriptionRepository).flush();
            ArgumentCaptor<NovelNotificationSubscription> captor =
                    ArgumentCaptor.forClass(NovelNotificationSubscription.class);
            inOrder.verify(subscriptionRepository).save(captor.capture());
            assertThat(captor.getValue().getNotificationType()).isEqualTo(NovelNotificationType.COMPLETION);
            assertThat(captor.getValue().isSent()).isFalse();
        }

        /** 이미 요청과 같은 상태면 저장도 삭제도 하지 않는지 검증한다. */
        @Test
        @DisplayName("이미 요청과 같은 상태면 아무것도 저장하거나 삭제하지 않는다")
        void doesNothingWhenAlreadyInRequestedState() {
            User user = createUser();
            Novel novel = createNovel();
            given(subscriptionRepository.findAllByUser_UserIdAndNovel_NovelId(USER_ID, NOVEL_ID))
                    .willReturn(List.of(createSubscription(NovelNotificationType.COMPLETION)));

            novelNotificationService.updateSettings(user, novel, true, false);

            verify(subscriptionRepository, never()).save(any(NovelNotificationSubscription.class));
            verify(subscriptionRepository, never()).delete(any(NovelNotificationSubscription.class));
        }
    }

    @Nested
    @DisplayName("작품 알림 설정 조회")
    class GetEnabledTypes {

        /** 저장된 구독에서 활성 알림 유형만 반환하는지 검증한다. */
        @Test
        @DisplayName("등록된 알림 유형 집합을 반환한다")
        void returnsEnabledTypes() {
            NovelNotificationSubscription completion = NovelNotificationSubscription.create(
                    createUser(),
                    org.mockito.Mockito.mock(Novel.class),
                    NovelNotificationType.COMPLETION
            );
            given(subscriptionRepository.findAllByUser_UserIdAndNovel_NovelId(USER_ID, NOVEL_ID))
                    .willReturn(List.of(completion));

            Set<NovelNotificationType> result = novelNotificationService.getEnabledTypes(USER_ID, NOVEL_ID);

            assertThat(result).containsExactly(NovelNotificationType.COMPLETION);
        }

        /** 발송이 끝난 구독은 토글이 꺼진 것으로 보이는지 검증한다. */
        @Test
        @DisplayName("발송이 끝난 구독은 활성 유형에서 제외한다")
        void excludesSentSubscription() {
            given(subscriptionRepository.findAllByUser_UserIdAndNovel_NovelId(USER_ID, NOVEL_ID))
                    .willReturn(List.of(
                            createSentSubscription(NovelNotificationType.COMPLETION),
                            createSubscription(NovelNotificationType.HIATUS_RETURN)
                    ));

            Set<NovelNotificationType> result = novelNotificationService.getEnabledTypes(USER_ID, NOVEL_ID);

            assertThat(result).containsExactly(NovelNotificationType.HIATUS_RETURN);
        }
    }

    @Nested
    @DisplayName("작품 알림 목록 조회와 삭제")
    class ManageSubscriptions {

        /** 커서와 크기를 Repository 페이지 요청으로 전달하는지 검증한다. */
        @Test
        @DisplayName("알림 유형별 구독 목록을 조회한다")
        void getsSubscriptionsByType() {
            Long lastSubscriptionId = 30L;
            int size = 10;
            PageRequest pageRequest = PageRequest.of(0, size);
            Slice<NovelNotificationSubscription> expected = new SliceImpl<>(List.of());
            given(subscriptionRepository.findSubscriptions(
                    USER_ID,
                    NovelNotificationType.HIATUS_RETURN,
                    lastSubscriptionId,
                    pageRequest
            )).willReturn(expected);

            Slice<NovelNotificationSubscription> result = novelNotificationService.getSubscriptions(
                    USER_ID,
                    NovelNotificationType.HIATUS_RETURN,
                    lastSubscriptionId,
                    size
            );

            assertThat(result).isSameAs(expected);
        }

        /** 사용자와 유형, 작품 목록을 제한 조건으로 전달하는지 검증한다. */
        @Test
        @DisplayName("선택한 작품의 같은 유형 구독을 일괄 삭제한다")
        void deletesSelectedSubscriptions() {
            List<Long> novelIds = List.of(10L, 20L);

            novelNotificationService.deleteSubscriptions(USER_ID, NovelNotificationType.COMPLETION, novelIds);

            verify(subscriptionRepository).deleteSubscriptions(
                    USER_ID,
                    NovelNotificationType.COMPLETION,
                    novelIds
            );
        }
    }

    private NovelNotificationSubscription createSentSubscription(NovelNotificationType notificationType) {
        NovelNotificationSubscription subscription = createSubscription(notificationType);
        ReflectionTestUtils.setField(subscription, "isSent", true);
        return subscription;
    }

    private NovelNotificationSubscription createSubscription(NovelNotificationType notificationType) {
        return NovelNotificationSubscription.create(
                createUser(),
                org.mockito.Mockito.mock(Novel.class),
                notificationType
        );
    }

    private User createUser() {
        User user = User.createBySocial("social-id", "사용자", null);
        ReflectionTestUtils.setField(user, "userId", USER_ID);
        return user;
    }

    private Novel createNovel() {
        Novel novel = org.mockito.Mockito.mock(Novel.class);
        given(novel.getNovelId()).willReturn(NOVEL_ID);
        return novel;
    }
}
