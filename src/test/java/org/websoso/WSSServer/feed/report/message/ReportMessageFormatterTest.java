package org.websoso.WSSServer.feed.report.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.feed.domain.FeedImage;
import org.websoso.WSSServer.user.domain.User;

class ReportMessageFormatterTest {

    private final ReportMessageFormatter formatter = new ReportMessageFormatter();

    @Nested
    @DisplayName("피드 신고 메시지")
    class FeedReportMessage {

        @Test
        void formatsFeedReportMessage() {
            User reporter = user(1L, "신고자");
            User writer = user(2L, "작성자");
            Feed feed = feed(10L, writer, "피드 내용");

            String content = formatter.formatFeedReportMessage(reporter, feed, ReportedType.SPOILER, 3, true);

            assertThat(content).contains("신고자", "작성자", "피드 내용", "총 3회");
        }
    }

    @Nested
    @DisplayName("댓글 신고 메시지")
    class CommentReportMessage {

        @Test
        void formatsCommentReportMessage() {
            User reporter = user(1L, "신고자");
            User writer = user(2L, "작성자");
            User commentWriter = user(3L, "댓글작성자");
            Feed feed = feed(10L, writer, "피드 내용");
            Comment comment = comment(30L, feed, commentWriter.getUserId(), "댓글 내용");

            String content = formatter.formatCommentReportMessage(
                    reporter,
                    feed,
                    comment,
                    ReportedType.IMPERTINENCE,
                    commentWriter,
                    2,
                    false
            );

            assertThat(content).contains("신고자", "작성자", "댓글작성자", "댓글 내용", "총 2회");
        }
    }

    private User user(Long userId, String nickname) {
        User user = User.createBySocial("social-" + userId, nickname, nickname + "@example.com");
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    private Feed feed(Long feedId, User writer, String content) {
        Feed feed = Feed.create(content, null, false, true, writer, List.of(FeedImage.createCommon("image.png", 1)));
        ReflectionTestUtils.setField(feed, "feedId", feedId);
        return feed;
    }

    private Comment comment(Long commentId, Feed feed, Long userId, String content) {
        Comment comment = Comment.create(feed, userId, content);
        ReflectionTestUtils.setField(comment, "commentId", commentId);
        return comment;
    }
}
