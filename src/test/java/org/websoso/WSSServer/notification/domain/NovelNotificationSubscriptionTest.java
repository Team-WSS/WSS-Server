package org.websoso.WSSServer.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.user.domain.User;

/** 작품 알림 구독 엔티티의 생성 규칙을 검증한다. */
class NovelNotificationSubscriptionTest {

    /** 사용자·작품·유형을 그대로 보존하는지 검증한다. */
    @Test
    @DisplayName("사용자와 작품, 알림 유형으로 구독을 생성한다")
    void createsSubscription() {
        User user = User.createBySocial("social-id", "사용자", null);
        Novel novel = org.mockito.Mockito.mock(Novel.class);

        NovelNotificationSubscription subscription = NovelNotificationSubscription.create(
                user,
                novel,
                NovelNotificationType.COMPLETION
        );

        assertThat(subscription.getUser()).isSameAs(user);
        assertThat(subscription.getNovel()).isSameAs(novel);
        assertThat(subscription.getNotificationType()).isEqualTo(NovelNotificationType.COMPLETION);
    }
}
