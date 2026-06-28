package org.websoso.WSSServer.feed.comment.repository;

import static org.websoso.WSSServer.feed.comment.domain.QComment.comment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

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
