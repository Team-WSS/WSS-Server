package org.websoso.WSSServer.feed.comment.repository;

import static org.websoso.WSSServer.feed.comment.domain.QComment.comment;
import static org.websoso.WSSServer.user.domain.QAvatarProfile.avatarProfile;
import static org.websoso.WSSServer.user.domain.QBlock.block;
import static org.websoso.WSSServer.user.domain.QUser.user;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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
    public List<CommentInfoRow> findCommentInfoRows(Long feedId, Long userId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        CommentInfoRow.class,
                        user.userId,
                        user.nickname,
                        avatarProfile.avatarProfileImage,
                        comment.commentId,
                        comment.createdDate,
                        comment.commentContent,
                        comment.createdDate.ne(comment.modifiedDate),
                        isMyComment(userId),
                        comment.isSpoiler,
                        isBlocked(userId),
                        comment.isHidden
                ))
                .from(comment)
                .join(user).on(comment.userId.eq(user.userId))
                .leftJoin(avatarProfile).on(user.avatarProfileId.eq(avatarProfile.avatarProfileId))
                .where(
                        comment.feed.feedId.eq(feedId),
                        excludeUsersWhoBlockedMe(userId)
                )
                .orderBy(comment.commentId.asc())
                .fetch();
    }

    private Expression<Boolean> isMyComment(Long userId) {
        if (userId == null) {
            return Expressions.FALSE;
        }

        return comment.userId.eq(userId);
    }

    private Expression<Boolean> isBlocked(Long userId) {
        if (userId == null) {
            return Expressions.FALSE;
        }

        return JPAExpressions
                .selectOne()
                .from(block)
                .where(
                        block.blockingId.eq(userId),
                        block.blockedId.eq(comment.userId)
                )
                .exists();
    }

    private BooleanExpression excludeUsersWhoBlockedMe(Long userId) {
        if (userId == null) {
            return null;
        }

        return comment.userId.notIn(
                JPAExpressions
                        .select(block.blockingId)
                        .from(block)
                        .where(block.blockedId.eq(userId))
        );
    }

}
