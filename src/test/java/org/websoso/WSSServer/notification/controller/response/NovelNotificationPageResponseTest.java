package org.websoso.WSSServer.notification.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.notification.domain.NovelNotificationSubscription;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

/** 작품 알림 구독 목록의 작품 정보·날짜·커서 변환을 검증한다. */
class NovelNotificationPageResponseTest {

    /** 다음 페이지가 있으면 마지막 구독 ID와 표시 정보를 반환하는지 검증한다. */
    @Test
    @DisplayName("구독 목록을 설정 화면 응답으로 변환한다")
    void convertsSubscriptionSlice() {
        NovelNotificationSubscription first = createSubscription(20L, 100L, "첫 작품", "첫 작가");
        NovelNotificationSubscription second = createSubscription(10L, 200L, "둘째 작품", "둘째 작가");
        Slice<NovelNotificationSubscription> slice = new SliceImpl<>(
                List.of(first, second),
                PageRequest.of(0, 2),
                true
        );

        NovelNotificationPageResponse response = NovelNotificationPageResponse.from(slice);

        assertThat(response.isLoadable()).isTrue();
        assertThat(response.nextSubscriptionId()).isEqualTo(10L);
        assertThat(response.subscriptions()).hasSize(2);
        assertThat(response.subscriptions().get(0))
                .extracting("novelId", "novelTitle", "novelAuthor", "registeredDate")
                .containsExactly(100L, "첫 작품", "첫 작가", "2026.07.14");
    }

    private NovelNotificationSubscription createSubscription(
            Long subscriptionId,
            Long novelId,
            String title,
            String author
    ) {
        Novel novel = mock(Novel.class);
        given(novel.getNovelId()).willReturn(novelId);
        given(novel.getNovelImage()).willReturn("image-" + novelId);
        given(novel.getTitle()).willReturn(title);
        given(novel.getAuthor()).willReturn(author);
        NovelNotificationSubscription subscription = NovelNotificationSubscription.create(
                mock(User.class),
                novel,
                NovelNotificationType.COMPLETION
        );
        ReflectionTestUtils.setField(
                subscription,
                "novelNotificationSubscriptionId",
                subscriptionId
        );
        ReflectionTestUtils.setField(subscription, "createdDate", LocalDateTime.of(2026, 7, 14, 12, 0));
        return subscription;
    }
}
