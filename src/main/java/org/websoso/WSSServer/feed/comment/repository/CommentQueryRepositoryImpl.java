package org.websoso.WSSServer.feed.comment.repository;

import static org.websoso.WSSServer.feed.comment.domain.QComment.comment;
import static org.websoso.WSSServer.user.domain.QAvatarProfile.avatarProfile;
import static org.websoso.WSSServer.user.domain.QUser.user;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.websoso.WSSServer.feed.comment.repository.projection.CommentInfoRow;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CommentInfoRow> findCommentInfoRows(Long feedId, Long userId, List<Long> blockedUserIds) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        CommentInfoRow.class,
                        user.userId,
                        user.nickname,
                        avatarProfile.avatarProfileImage,
                        comment.commentId,
                        comment.createdDate,
                        comment.content,
                        comment.createdDate.ne(comment.modifiedDate),
                        isMyComment(userId),
                        comment.isSpoiler,
                        isBlocked(),
                        comment.isHidden
                ))
                .from(comment)
                .join(user).on(comment.userId.eq(user.userId))
                .leftJoin(avatarProfile).on(user.avatarProfileId.eq(avatarProfile.avatarProfileId))
                .where(
                        comment.feed.feedId.eq(feedId),
                        excludeBlockedUsers(blockedUserIds)
                )
                .orderBy(comment.commentId.asc())
                .fetch();
    }

    @Override
    public long countVisibleComments(Long feedId, List<Long> blockedUserIds) {
        Long count = jpaQueryFactory
                .select(comment.commentId.count())
                .from(comment)
                .where(
                        comment.feed.feedId.eq(feedId),
                        excludeBlockedUsers(blockedUserIds)
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    private Expression<Boolean> isMyComment(Long userId) {
        if (userId == null) {
            return Expressions.FALSE;
        }

        return comment.userId.eq(userId);
    }

    private Expression<Boolean> isBlocked() {
        return Expressions.FALSE;
    }

    private BooleanExpression excludeBlockedUsers(List<Long> blockedUserIds) {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            return null;
        }

        return comment.userId.notIn(blockedUserIds);
    }

}
