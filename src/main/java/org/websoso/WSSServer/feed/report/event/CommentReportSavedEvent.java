package org.websoso.WSSServer.feed.report.event;

import org.websoso.WSSServer.domain.common.ReportedType;

public record CommentReportSavedEvent(
        Long reporterId,
        Long commentId,
        ReportedType reportedType
) {

    public static CommentReportSavedEvent of(Long reporterId, Long commentId, ReportedType reportedType) {
        return new CommentReportSavedEvent(reporterId, commentId, reportedType);
    }
}
