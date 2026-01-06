package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomAuthorizationError implements ICustomError {

    UNSUPPORTED_RESOURCE_TYPE("AUTHORIZATION-001", "지원하지 않는 리소스 타입입니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
