package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.ReportedType.IMPERTINENCE;
import static org.websoso.WSSServer.domain.common.ReportedType.SPOILER;

import org.websoso.WSSServer.domain.Comment;
import org.websoso.WSSServer.domain.Feed;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.DiscordMessageTemplate;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.domain.common.SocialLoginType;

public class MessageFormatter {

    public static String formatFeedReportMessage(User user, Feed feed, ReportedType reportedType, int reportedCount,
                                                 boolean isHidden) {
        String hiddenMessage = isHidden
                ? "해당 피드는 숨김 처리되었습니다."
                : "해당 피드는 숨김 처리되지 않았습니다.";
        return String.format(
                DiscordMessageTemplate.FEED_REPORT.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                reportedType.getDescription(),
                user.getUserId(),
                user.getNickname(),
                feed.getUser().getUserId(),
                feed.getUser().getNickname(),
                feed.getFeedContent(),
                reportedCount,
                hiddenMessage
        );
    }

    public static String formatCommentReportMessage(User user, String feedContent, Comment comment,
                                                    ReportedType reportedType, User commentCreatedUser,
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
                DiscordMessageTemplate.COMMENT_REPORT.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                reportedType.getDescription(),
                user.getUserId(),
                user.getNickname(),
                commentCreatedUser.getUserId(),
                commentCreatedUser.getNickname(),
                feedContent,
                comment.getCommentContent(),
                reportedCount,
                hiddenMessage
        );
    }

    public static String formatUserWithdrawMessage(Long userId, String userNickname, String reason) {
        return String.format(
                DiscordMessageTemplate.USER_WITHDRAW.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                userId,
                userNickname,
                reason
        );
    }

    public static String formatUserJoinMessage(User user, SocialLoginType socialLoginType) {
        return String.format(
                DiscordMessageTemplate.USER_JOIN.getTemplate(),
                user.getCreatedDate(),
                socialLoginType.getLabel(),
                user.getUserId()
        );
    }
}
