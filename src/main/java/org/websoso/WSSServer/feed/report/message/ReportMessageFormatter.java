package org.websoso.WSSServer.feed.report.message;

import org.springframework.stereotype.Component;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.notification.service.MessageFormatter;
import org.websoso.WSSServer.user.domain.User;

@Component
public class ReportMessageFormatter {

    public String formatFeedReportMessage(User reporter, Feed feed, ReportedType reportedType,
                                          int reportedCount, boolean hidden) {
        return MessageFormatter.formatFeedReportMessage(
                reporter,
                feed,
                reportedType,
                reportedCount,
                hidden
        );
    }

    public String formatCommentReportMessage(User reporter, Feed feed, Comment comment, ReportedType reportedType,
                                             User commentCreatedUser, int reportedCount, boolean hidden) {
        return MessageFormatter.formatCommentReportMessage(
                reporter,
                feed,
                comment,
                reportedType,
                commentCreatedUser,
                reportedCount,
                hidden
        );
    }
}
