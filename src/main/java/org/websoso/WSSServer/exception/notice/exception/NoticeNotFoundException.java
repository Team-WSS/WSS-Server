package org.websoso.WSSServer.exception.notice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.notice.NoticeErrorCode;

@Getter
@AllArgsConstructor
public class NoticeNotFoundException extends RuntimeException {

    public NoticeNotFoundException(NoticeErrorCode noticeErrorCode, String message) {
        super(message);
        this.noticeErrorCode = noticeErrorCode;
    }

    private NoticeErrorCode noticeErrorCode;
}
