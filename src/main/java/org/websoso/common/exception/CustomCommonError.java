package org.websoso.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 특정 도메인에 속하지 않는 요청 처리 오류의 공통 서비스 에러 코드.
 * 어떤 엔드포인트에서든 같은 원인이면 같은 코드로 응답하도록 {@code COMMON-} 접두사 아래에 모아 정의한다.
 *
 * <p>도메인 비즈니스 규칙 위반은 여기에 넣지 않는다. 그런 오류는 원인이 도메인마다 다르므로
 * {@code CustomCollectionError}처럼 도메인별 {@link ICustomError} 정의를 사용한다.
 *
 * <p>{@link #INVALID_REQUEST_FIELD}처럼 실패한 검증 애너테이션의 메시지를 그대로 내려주는 오류는
 * 응답 {@code message}가 요청마다 다르다. 이때 {@code description}은 어떤 필드가 실패했는지 알 수 없을 때
 * 쓰는 기본 메시지이며, 실제 응답 메시지는 {@code GlobalExceptionHandler}가 예외에서 뽑아 조합한다.
 */
@Getter
@AllArgsConstructor
public enum CustomCommonError implements ICustomError {

    INVALID_REQUEST_FIELD("COMMON-001", "요청 필드 검증에 실패했습니다.", BAD_REQUEST),
    MALFORMED_REQUEST_BODY("COMMON-002", "잘못된 JSON 형식입니다.", BAD_REQUEST);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
