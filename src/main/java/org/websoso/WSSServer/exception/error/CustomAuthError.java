package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomAuthError implements ICustomError {

    INVALID_TOKEN("AUTH-001", "유효하지 않은 토큰입니다.", UNAUTHORIZED),
    UNSUPPORTED_SOCIAL_LOGIN_TYPE("AUTH-002", "지원되지 않는 소셜 로그인 유형입니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
