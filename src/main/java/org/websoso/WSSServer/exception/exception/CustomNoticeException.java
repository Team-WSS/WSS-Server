package org.websoso.WSSServer.exception.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.error.NoticeErrorCode;

@Getter
@AllArgsConstructor
public class CustomNoticeException extends RuntimeException {

    public CustomNoticeException(NoticeErrorCode noticeErrorCode, String message) {
        super(message);
        this.noticeErrorCode = noticeErrorCode;
    }

    private NoticeErrorCode noticeErrorCode;
}
