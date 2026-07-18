package org.websoso.WSSServer.feed.comment.repository;

import static org.websoso.WSSServer.feed.comment.domain.QComment.comment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean markSpoilerIfNotMarked(Long commentId) {
        return jpaQueryFactory
                .update(comment)
                .set(comment.isSpoiler, true)
                .where(
                        comment.commentId.eq(commentId),
                        comment.isSpoiler.isFalse()
                )
                .execute() > 0;
    }

    @Override
    public boolean hideIfNotHidden(Long commentId) {
        return jpaQueryFactory
                .update(comment)
                .set(comment.isHidden, true)
                .where(
                        comment.commentId.eq(commentId),
                        comment.isHidden.isFalse()
                )
                .execute() > 0;
    }

    @Override
    public void updateUserToUnknown(Long userId) {
        jpaQueryFactory
                .update(comment)
                .set(comment.userId, -1L)
                .where(comment.userId.eq(userId))
                .execute();
    }

    @Override
    public void deleteByFeedId(Long feedId) {
        jpaQueryFactory
                .delete(comment)
                .where(comment.feed.feedId.eq(feedId))
                .execute();
    }
}
