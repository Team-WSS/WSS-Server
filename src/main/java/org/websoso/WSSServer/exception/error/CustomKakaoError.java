package org.websoso.WSSServer.exception.error;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.websoso.common.exception.ICustomError;

@Getter
@AllArgsConstructor
public enum CustomKakaoError implements ICustomError {

    INVALID_KAKAO_ACCESS_TOKEN("KAKAO-001", "카카오에서 제공한 access token이 유효하지 않습니다.", UNAUTHORIZED),
    KAKAO_SERVER_ERROR("KAKAO-002", "카카오 서버 오류", SERVICE_UNAVAILABLE);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;
}
