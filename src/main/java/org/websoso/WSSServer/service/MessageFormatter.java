package org.websoso.WSSServer.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.ReportedType;

public class MessageFormatter {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private static final String FEED_REPORT_MESSAGE =
            "```[%s] 피드 %s 신고가 접수되었습니다.\n\n" +
                    "[신고된 피드 작성자]\n" +
                    "유저 아이디 : %d\n" +
                    "유저 닉네임 : %s\n\n" +
                    "[신고된 피드 내용]\n%s\n```";

    private static final String COMMENT_REPORT_MESSAGE =
            "```[%s] 피드 댓글 %s 신고가 접수되었습니다.\n\n" +
                    "[신고된 댓글 작성자]\n" +
                    "유저 아이디 : %d\n" +
                    "유저 닉네임 : %s\n\n" +
                    "[신고된 댓글 내용]\n%s\n```";

    public static String formatFeedReportMessage(Feed feed, ReportedType reportedType) {
        return String.format(
                FEED_REPORT_MESSAGE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                reportedType.getDescription(),
                feed.getUser().getUserId(),
                feed.getUser().getNickname(),
                feed.getFeedContent()
        );
    }

    public static String formatCommentReportMessage(Comment comment, ReportedType reportedType, User user) {
        return String.format(
                COMMENT_REPORT_MESSAGE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                reportedType.getDescription(),
                user.getUserId(),
                user.getNickname(),
                comment.getCommentContent()
        );
    }

}
