package org.websoso.WSSServer.notification.repository;

import static org.websoso.WSSServer.notification.domain.QNovelNotificationSubscription.novelNotificationSubscription;
import static org.websoso.WSSServer.novel.domain.QNovel.novel;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.notification.domain.NovelNotificationSubscription;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;

/**
 * 작품 알림 구독의 목록 조회와 삭제를 QueryDSL로 수행한다.
 * QueryDSL에는 @Modifying의 flushAutomatically/clearAutomatically가 적용되지 않아,
 * 벌크 삭제 전후로 영속성 컨텍스트를 직접 flush/clear 해 기존 동작을 유지한다.
 */
@Repository
@RequiredArgsConstructor
public class NovelNotificationSubscriptionCustomRepositoryImpl implements NovelNotificationSubscriptionCustomRepository {

    private static final long NO_CURSOR = 0L;

    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Override
    public Slice<NovelNotificationSubscription> findSubscriptions(
            Long userId,
            NovelNotificationType notificationType,
            Long lastSubscriptionId,
            Pageable pageable
    ) {
        List<NovelNotificationSubscription> subscriptions = jpaQueryFactory
                .selectFrom(novelNotificationSubscription)
                .join(novelNotificationSubscription.novel, novel).fetchJoin()
                .where(
                        novelNotificationSubscription.user.userId.eq(userId),
                        novelNotificationSubscription.notificationType.eq(notificationType),
                        novelNotificationSubscription.isSent.isFalse(),
                        ltSubscriptionId(lastSubscriptionId)
                )
                .orderBy(novelNotificationSubscription.novelNotificationSubscriptionId.desc())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = subscriptions.size() > pageable.getPageSize();

        if (hasNext) {
            subscriptions.remove(subscriptions.size() - 1);
        }

        return new SliceImpl<>(subscriptions, pageable, hasNext);
    }

    /** 첫 페이지 요청(커서 0)에는 커서 조건을 붙이지 않는다. */
    private BooleanExpression ltSubscriptionId(Long lastSubscriptionId) {
        if (lastSubscriptionId == null || lastSubscriptionId == NO_CURSOR) {
            return null;
        }

        return novelNotificationSubscription.novelNotificationSubscriptionId.lt(lastSubscriptionId);
    }

    @Override
    public int deleteSubscriptions(Long userId, NovelNotificationType notificationType, List<Long> novelIds) {
        entityManager.flush();

        long deletedCount = jpaQueryFactory
                .delete(novelNotificationSubscription)
                .where(
                        novelNotificationSubscription.user.userId.eq(userId),
                        novelNotificationSubscription.notificationType.eq(notificationType),
                        novelNotificationSubscription.novel.novelId.in(novelIds)
                )
                .execute();

        entityManager.clear();

        return Math.toIntExact(deletedCount);
    }
}
