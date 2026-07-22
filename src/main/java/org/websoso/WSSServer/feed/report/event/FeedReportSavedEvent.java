package org.websoso.WSSServer.feed.report.event;

import org.websoso.WSSServer.domain.common.ReportedType;

public record FeedReportSavedEvent(
        Long reporterId,
        Long feedId,
        ReportedType reportedType
) {

    public static FeedReportSavedEvent of(Long reporterId, Long feedId, ReportedType reportedType) {
        return new FeedReportSavedEvent(reporterId, feedId, reportedType);
    }
}
