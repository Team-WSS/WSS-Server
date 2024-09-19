package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;

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
                    "[신고된 피드 내용]\n%s\n\n" +
                    "[신고 횟수]\n총 신고 횟수 %d회.\n" +
                    "%s\n```";

    private static final String COMMENT_REPORT_MESSAGE =
            "```[%s] 피드 댓글 %s 신고가 접수되었습니다.\n\n" +
                    "[신고된 댓글 작성자]\n" +
                    "유저 아이디 : %d\n" +
                    "유저 닉네임 : %s\n\n" +
                    "[신고된 댓글 내용]\n%s\n\n" +
                    "[신고 횟수]\n총 신고 횟수 %d회.\n" +
                    "%s\n```";

    public static String formatFeedReportMessage(Feed feed, ReportedType reportedType, int reportedCount,
                                                 boolean isHidden) {
        String hiddenMessage = isHidden ? "해당 피드는 숨김 처리되었습니다." : "해당 피드는 숨김 처리되지 않았습니다.";

        return String.format(
                FEED_REPORT_MESSAGE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                reportedType.getDescription(),
                feed.getUser().getUserId(),
                feed.getUser().getNickname(),
                feed.getFeedContent(),
                reportedCount,
                hiddenMessage
        );
    }

    public static String formatCommentReportMessage(Comment comment, ReportedType reportedType, User user,
                                                    int reportedCount, boolean isHidden) {
        String hiddenMessage = "해당 댓글은 현재 숨김 처리되지 않은 상태입니다.";
        if (isHidden) {
            if (reportedType.equals(SPOILER)) {
                hiddenMessage = "해당 댓글은 스포일러 댓글로 지정되었습니다.";
            } else if (reportedType.equals(IMPERTINENCE)) {
                hiddenMessage = "해당 댓글은 부적절한 내용으로 인해 숨김 처리되었습니다.";
            }
        }

        return String.format(
                COMMENT_REPORT_MESSAGE,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                reportedType.getDescription(),
                user.getUserId(),
                user.getNickname(),
                comment.getCommentContent(),
                reportedCount,
                hiddenMessage
        );
    }

}
