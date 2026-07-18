package org.websoso.WSSServer.feed.report.message;

import static org.websoso.WSSServer.infrastructure.discord.DiscordMessageTemplate.COMMENT_REPORT;
import static org.websoso.WSSServer.infrastructure.discord.DiscordMessageTemplate.FEED_REPORT;

import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.report.domain.ReportModerationAction;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageTemplate;
import org.websoso.WSSServer.user.domain.User;

@Component
public class ReportMessageFormatter {

    public String formatFeedReportMessage(User reporter, Feed feed, ReportedType reportedType,
                                          int reportedCount, ReportModerationAction moderationAction) {
        return String.format(
                FEED_REPORT.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                reportedType.getDescription(),
                reporter.getNickname(),
                reporter.getUserId(),
                feed.getUser().getNickname(),
                feed.getWriterId(),
                feed.getFeedContent(),
                reportedCount,
                feedModerationMessage(reportedType, moderationAction)
        );
    }

    public String formatCommentReportMessage(User reporter, Feed feed, Comment comment, User commentWriter,
                                             ReportedType reportedType, int reportedCount,
                                             ReportModerationAction moderationAction) {
        return String.format(
                COMMENT_REPORT.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                reportedType.getDescription(),
                reporter.getNickname(),
                reporter.getUserId(),
                commentWriter.getNickname(),
                commentWriter.getUserId(),
                comment.getContent(),
                feed.getUser().getNickname(),
                feed.getWriterId(),
                feed.getFeedContent(),
                reportedCount,
                commentModerationMessage(moderationAction)
        );
    }

    private String feedModerationMessage(ReportedType reportedType, ReportModerationAction moderationAction) {
        if (moderationAction == ReportModerationAction.MARKED_AS_SPOILER) {
            return "해당 글은 스포일러 글로 지정되었습니다.";
        }

        if (moderationAction == ReportModerationAction.HIDDEN) {
            return "해당 글은 숨김 처리되었습니다.";
        }

        return reportedType.isSpoiler()
                ? "해당 글은 스포일러 글로 지정되지 않았습니다."
                : "해당 글은 숨김 처리되지 않았습니다.";
    }

    private String commentModerationMessage(ReportModerationAction moderationAction) {
        return switch (moderationAction) {
            case MARKED_AS_SPOILER -> "해당 댓글은 스포일러 댓글로 지정되었습니다.";
            case HIDDEN -> "해당 댓글은 부적절한 내용으로 인해 숨김 처리되었습니다.";
            case NONE -> "해당 댓글은 현재 숨김 처리되지 않은 상태입니다.";
        };
    }
}
