package org.websoso.WSSServer.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.domain.common.SocialLoginType;
import org.websoso.WSSServer.feed.domain.Comment;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.user.domain.User;

class MessageFormatterTest {

    @Nested
    @DisplayName("피드 신고 메시지 포맷팅 테스트")
    class FormatFeedReportMessage {

        @DisplayName("숨김 처리된 피드 신고 메시지를 포맷팅한다")
        @Test
        void formatsHiddenFeedReportMessage() {
            // given
            User reporter = createUser(1L, "신고자");
            User feedOwner = createUser(2L, "글쓴이");
            Feed feed = createFeed(100L, feedOwner, "신고될 글 내용");
            ReportedType reportedType = ReportedType.SPOILER;
            int reportedCount = 5;
            boolean isHidden = true;

            // when
            String result = MessageFormatter.formatFeedReportMessage(
                    reporter, feed, reportedType, reportedCount, isHidden);

            // then
            assertThat(result)
                    .contains("스포일러")
                    .contains("신고자")
                    .contains("글쓴이")
                    .contains("신고될 글 내용")
                    .contains("숨김 처리");
        }

        @DisplayName("숨김 처리되지 않은 피드 신고 메시지를 포맷팅한다")
        @Test
        void formatsNonHiddenFeedReportMessage() {
            // given
            User reporter = createUser(1L, "신고자");
            User feedOwner = createUser(2L, "글쓴이");
            Feed feed = createFeed(100L, feedOwner, "신고될 글 내용");
            ReportedType reportedType = ReportedType.IMPERTINENCE;
            int reportedCount = 3;
            boolean isHidden = false;

            // when
            String result = MessageFormatter.formatFeedReportMessage(
                    reporter, feed, reportedType, reportedCount, isHidden);

            // then
            assertThat(result)
                    .contains("부적절한 표현")
                    .contains("숨김 처리되지 않았습니다");
        }
    }

    @Nested
    @DisplayName("댓글 신고 메시지 포맷팅 테스트")
    class FormatCommentReportMessage {

        @DisplayName("숨김 처리된 댓글 신고 메시지를 포맷팅한다")
        @Test
        void formatsHiddenCommentReportMessage() {
            // given
            User reporter = createUser(1L, "신고자");
            User commentOwner = createUser(3L, "댓글쓴이");
            User feedOwner = createUser(2L, "글쓴이");
            Feed feed = createFeed(100L, feedOwner, "글 내용");
            Comment comment = createComment(200L, commentOwner, feed, "신고될 댓글");
            ReportedType reportedType = ReportedType.SPOILER;
            int reportedCount = 2;
            boolean isHidden = true;

            // when
            String result = MessageFormatter.formatCommentReportMessage(
                    reporter, feed, comment, reportedType, commentOwner, reportedCount, isHidden);

            // then
            assertThat(result)
                    .contains("스포일러")
                    .contains("스포일러 댓글로 지정");
        }

        @DisplayName("부적절한 댓글 신고 메시지를 포맷팅한다")
        @Test
        void formatsImpertinentCommentReportMessage() {
            // given
            User reporter = createUser(1L, "신고자");
            User commentOwner = createUser(3L, "댓글쓴이");
            User feedOwner = createUser(2L, "글쓴이");
            Feed feed = createFeed(100L, feedOwner, "글 내용");
            Comment comment = createComment(200L, commentOwner, feed, "부적절한 댓글");
            ReportedType reportedType = ReportedType.IMPERTINENCE;
            int reportedCount = 1;
            boolean isHidden = true;

            // when
            String result = MessageFormatter.formatCommentReportMessage(
                    reporter, feed, comment, reportedType, commentOwner, reportedCount, isHidden);

            // then
            assertThat(result)
                    .contains("부적절한 표현")
                    .contains("부적절한 내용으로 인해 숨김 처리");
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 메시지 포맷팅 테스트")
    class FormatUserWithdrawMessage {

        @DisplayName("회원탈퇴 메시지를 포맷팅한다")
        @Test
        void formatsUserWithdrawMessage() {
            // given
            Long userId = 1L;
            String userNickname = "탈퇴하는사용자";
            String reason = "개인사정";

            // when
            String result = MessageFormatter.formatUserWithdrawMessage(userId, userNickname, reason);

            // then
            assertThat(result)
                    .contains("탈퇴하는사용자")
                    .contains("개인사정");
        }
    }

    @Nested
    @DisplayName("회원 가입 메시지 포맷팅 테스트")
    class FormatUserJoinMessage {

        @DisplayName("회원가입 메시지를 포맷팅한다")
        @Test
        void formatsUserJoinMessage() {
            // given
            User user = createUser(1L, "새로운사용자");
            SocialLoginType socialLoginType = SocialLoginType.KAKAO;

            // when
            String result = MessageFormatter.formatUserJoinMessage(user, socialLoginType);

            // then
            assertThat(result)
                    .contains("카카오")
                    .contains("새로운사용자");
        }
    }

    private User createUser(Long userId, String nickname) {
        User user = User.createBySocial("social-" + userId, nickname, null);
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    private Feed createFeed(Long feedId, User owner, String content) {
        Feed feed = Feed.create(content, null, false, true, owner, List.of());
        ReflectionTestUtils.setField(feed, "feedId", feedId);
        return feed;
    }

    private Comment createComment(Long commentId, User owner, Feed feed, String content) {
        Comment comment = Comment.create(owner.getUserId(), feed, content);
        ReflectionTestUtils.setField(comment, "commentId", commentId);
        return comment;
    }
}
