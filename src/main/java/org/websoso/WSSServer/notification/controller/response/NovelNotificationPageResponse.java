package org.websoso.WSSServer.notification.controller.response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.notification.domain.NovelNotificationSubscription;
import org.websoso.WSSServer.novel.domain.Novel;

/** 설정 화면의 작품 알림 구독 목록과 다음 페이지 정보를 반환한다. */
public record NovelNotificationPageResponse(
        boolean isLoadable,
        Long nextSubscriptionId,
        List<NovelNotificationItem> subscriptions
) {

    /** 구독 Slice를 설정 화면 응답으로 변환한다. */
    public static NovelNotificationPageResponse from(Slice<NovelNotificationSubscription> subscriptionSlice) {
        List<NovelNotificationItem> items = subscriptionSlice.getContent().stream()
                .map(NovelNotificationItem::from)
                .toList();
        Long nextSubscriptionId = subscriptionSlice.hasNext() && !items.isEmpty()
                ? items.get(items.size() - 1).subscriptionId()
                : null;

        return new NovelNotificationPageResponse(
                subscriptionSlice.hasNext(),
                nextSubscriptionId,
                items
        );
    }

    /** 설정 목록 한 항목에 필요한 작품 정보와 등록일을 반환한다. */
    public record NovelNotificationItem(
            Long subscriptionId,
            Long novelId,
            String novelImage,
            String novelTitle,
            String novelAuthor,
            String registeredDate
    ) {
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        /** 작품 알림 구독을 목록 항목으로 변환한다. */
        public static NovelNotificationItem from(NovelNotificationSubscription subscription) {
            Novel novel = subscription.getNovel();

            return new NovelNotificationItem(
                    subscription.getNovelNotificationSubscriptionId(),
                    novel.getNovelId(),
                    novel.getNovelImage(),
                    novel.getTitle(),
                    novel.getAuthor(),
                    subscription.getCreatedDate().format(DATE_FORMATTER)
            );
        }
    }
}
