package org.websoso.WSSServer.exception.platform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.WSSServer.exception.common.IErrorCode;

@Getter
@AllArgsConstructor
public enum PlatformErrorCode implements IErrorCode {

    PLATFORM_NOT_FOUND("PLATFORM-001", "해당 작품의 플랫폼을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
