package org.websoso.WSSServer.exception.block;

import static org.springframework.http.HttpStatus.CONFLICT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@AllArgsConstructor
@Getter
public enum BlockErrorCode implements IErrorCode {

    ALREADY_BLOCKED("BLOCK-001", "이미 차단한 계정입니다.", CONFLICT);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
