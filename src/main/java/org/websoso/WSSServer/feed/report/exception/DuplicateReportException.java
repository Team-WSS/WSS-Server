package org.websoso.WSSServer.feed.report.exception;

public class DuplicateReportException extends CustomReportException {

    public DuplicateReportException(CustomReportError customReportError, String message) {
        super(customReportError, message);
    }
}
