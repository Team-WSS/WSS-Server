package org.websoso.WSSServer.notification.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.websoso.WSSServer.notification.domain.NovelNotificationSubscription;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;

/** 작품 알림 구독의 목록 조회와 삭제를 QueryDSL로 처리한다. */
public interface NovelNotificationSubscriptionCustomRepository {

    /** 알림 유형별로 아직 발송되지 않은 등록 작품을 최신 구독 ID 순으로 조회한다. */
    Slice<NovelNotificationSubscription> findSubscriptions(
            Long userId,
            NovelNotificationType notificationType,
            Long lastSubscriptionId,
            Pageable pageable
    );

    /** 사용자가 선택한 같은 유형의 작품 알림을 한 번에 삭제한다. */
    int deleteSubscriptions(Long userId, NovelNotificationType notificationType, List<Long> novelIds);
}
