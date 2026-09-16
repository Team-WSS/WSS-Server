package org.websoso.WSSServer.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.websoso.WSSServer.notification.domain.NovelNotificationSubscription;

/** 작품 알림 구독의 사용자별 조회와 저장을 담당한다. */
public interface NovelNotificationSubscriptionRepository
        extends JpaRepository<NovelNotificationSubscription, Long>, NovelNotificationSubscriptionCustomRepository {

    /** 한 작품에 등록된 사용자의 모든 알림 유형을 조회한다. */
    List<NovelNotificationSubscription> findAllByUser_UserIdAndNovel_NovelId(Long userId, Long novelId);

}
