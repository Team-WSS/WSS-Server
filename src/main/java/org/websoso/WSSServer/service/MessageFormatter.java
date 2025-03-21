package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.DiscordMessageTemplate.COMMENT_REPORT;
import static org.websoso.WSSServer.domain.common.DiscordMessageTemplate.FEED_REPORT;
import static org.websoso.WSSServer.domain.common.DiscordMessageTemplate.USER_JOIN;
import static org.websoso.WSSServer.domain.common.DiscordMessageTemplate.USER_WITHDRAW;
import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;

import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.DiscordMessageTemplate;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.domain.common.SocialLoginType;

public class MessageFormatter {

    public static String formatFeedReportMessage(User user, Feed feed, ReportedType reportedType, int reportedCount) {
        String hiddenMessage = reportedCount >= 3
                ? "해당 수다는 숨김 처리되었습니다."
                : "해당 수다는 숨김 처리되지 않았습니다.";
        return String.format(
                FEED_REPORT.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                reportedType.getDescription(),
                user.getNickname(),
                user.getUserId(),
                feed.getUser().getNickname(),
                feed.getUser().getUserId(),
                feed.getFeedContent(),
                reportedCount,
                hiddenMessage
        );
    }

    public static String formatCommentReportMessage(User user, Feed feed, Comment comment,
                                                    ReportedType reportedType, User commentCreatedUser,
                                                    int reportedCount) {
        String hiddenMessage = "해당 댓글은 현재 숨김 처리되지 않은 상태입니다.";
        if (reportedCount >= 3) {
            if (reportedType.equals(SPOILER)) {
                hiddenMessage = "해당 댓글은 스포일러 댓글로 지정되었습니다.";
            } else if (reportedType.equals(IMPERTINENCE)) {
                hiddenMessage = "해당 댓글은 부적절한 내용으로 인해 숨김 처리되었습니다.";
            }
        }
        return String.format(
                COMMENT_REPORT.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                reportedType.getDescription(),
                user.getNickname(),
                user.getUserId(),
                commentCreatedUser.getNickname(),
                commentCreatedUser.getUserId(),
                comment.getCommentContent(),
                feed.getUser().getNickname(),
                feed.getUser().getUserId(),
                feed.getFeedContent(),
                reportedCount,
                hiddenMessage
        );
    }

    public static String formatUserWithdrawMessage(Long userId, String userNickname, String reason) {
        return String.format(
                USER_WITHDRAW.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                userNickname,
                userId,
                reason
        );
    }

    public static String formatUserJoinMessage(User user, SocialLoginType socialLoginType) {
        return String.format(
                USER_JOIN.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                socialLoginType.getLabel(),
                user.getNickname(),
                user.getUserId()
        );
    }
}
