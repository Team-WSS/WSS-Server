package org.websoso.WSSServer.feed.report.exception;

import lombok.Getter;
import org.websoso.common.exception.AbstractCustomException;

@Getter
public class CustomReportException extends AbstractCustomException {

    public CustomReportException(CustomReportError customReportError, String message) {
        super(customReportError, message);
    }
}
