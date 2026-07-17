package org.websoso.WSSServer.feed.report.repository;

import static org.websoso.WSSServer.feed.report.domain.QReportedComment.reportedComment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.websoso.WSSServer.feed.comment.domain.Comment;

@RequiredArgsConstructor
public class ReportedCommentCustomRepositoryImpl implements ReportedCommentCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void deleteByComment(Comment comment) {
        jpaQueryFactory
                .delete(reportedComment)
                .where(reportedComment.comment.eq(comment))
                .execute();
    }

    @Override
    public void deleteByCommentIdsIn(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return;
        }

        jpaQueryFactory
                .delete(reportedComment)
                .where(reportedComment.comment.commentId.in(commentIds))
                .execute();
    }

    @Override
    public void deleteByFeedId(Long feedId) {
        jpaQueryFactory
                .delete(reportedComment)
                .where(reportedComment.comment.feed.feedId.eq(feedId))
                .execute();
    }
}
