package org.websoso.WSSServer.notification.repository;

import static org.websoso.WSSServer.notification.domain.QNotification.notification;
import static org.websoso.WSSServer.notification.domain.QReadNotification.readNotification;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.notification.dto.ReadNotificationDto;

@Repository
@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsUnreadNotifications(Long userId) {
        return jpaQueryFactory
                .selectOne()
                .from(notification)
                .leftJoin(readNotification)
                .on(
                        notification.eq(readNotification.notification),
                        readNotification.user.userId.eq(userId)
                )
                .where(
                        notification.userId.in(userId, 0L),
                        readNotification.readNotificationId.isNull()
                )
                .fetchFirst() != null;
    }

    @Override
    public Slice<ReadNotificationDto> findNotifications(Long lastNotificationId, Long userId, Pageable pageable) {

        List<ReadNotificationDto> content = jpaQueryFactory
                .select(Projections.constructor(ReadNotificationDto.class,
                        notification,
                        readNotification.readNotificationId.isNotNull()
                ))
                .from(notification)
                .leftJoin(readNotification)
                .on(
                        readNotification.notification.eq(notification),
                        readNotification.user.userId.eq(userId))
                .where(
                        ltNotificationId(lastNotificationId),
                        notification.userId.eq(userId).or(notification.userId.eq(0L))
                )
                .orderBy(notification.notificationId.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return toSlice(content, pageable);
    }

    private BooleanExpression ltNotificationId(Long lastNotificationId) {
        if (lastNotificationId == null || lastNotificationId == 0) {
            return null;
        }

        return notification.notificationId.lt(lastNotificationId);
    }

    private Slice<ReadNotificationDto> toSlice(List<ReadNotificationDto> content, Pageable pageable) {
        boolean hasNext = false;

        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

}
