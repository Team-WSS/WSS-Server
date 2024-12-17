package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.ICustomError;

@AllArgsConstructor
@Getter
public enum CustomMinimumVersionError implements ICustomError {

    OS_NOT_FOUND("MINIMUM-VERSION-001", "요청한 OS 값을 찾을 수 없습니다.", NOT_FOUND),
    MINIMUM_VERSION_NOT_FOUND("MINIMUM-VERSION-002", "해당 OS에 대한 최소 지원 버전을 찾을 수 없습니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
