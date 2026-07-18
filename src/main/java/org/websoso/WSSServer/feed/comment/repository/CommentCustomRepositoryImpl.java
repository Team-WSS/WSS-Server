package org.websoso.WSSServer.feed.comment.repository;

import static org.websoso.WSSServer.feed.comment.domain.QComment.comment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void markSpoiler(Long commentId) {
        jpaQueryFactory
                .update(comment)
                .set(comment.isSpoiler, true)
                .where(comment.commentId.eq(commentId))
                .execute();
    }

    @Override
    public void hide(Long commentId) {
        jpaQueryFactory
                .update(comment)
                .set(comment.isHidden, true)
                .where(comment.commentId.eq(commentId))
                .execute();
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
