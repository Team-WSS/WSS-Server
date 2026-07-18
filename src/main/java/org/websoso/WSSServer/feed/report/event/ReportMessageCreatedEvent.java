package org.websoso.WSSServer.feed.report.event;

public record ReportMessageCreatedEvent(
        String content
) {

    public static ReportMessageCreatedEvent of(String content) {
        return new ReportMessageCreatedEvent(content);
    }
}
