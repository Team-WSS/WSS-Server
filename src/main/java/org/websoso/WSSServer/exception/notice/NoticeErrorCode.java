package org.websoso.WSSServer.exception.notice;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum NoticeErrorCode implements IErrorCode {

    NOTICE_FORBIDDEN("NOTICE-001", "관리자가 아닌 계정은 공지사항을 작성할 수 없습니다.", FORBIDDEN);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
