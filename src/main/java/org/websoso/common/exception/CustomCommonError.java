package org.websoso.common.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

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
 *
 * <p>{@link #UNEXPECTED_SERVER_ERROR}만은 예외로 동적 메시지를 쓰지 않는다. 원인이 되는 예외 메시지에는
 * SQL이나 내부 구조가 섞일 수 있어, 응답에는 고정 {@code description}만 내려주고 원본은 로그에만 남긴다.
 */
@Getter
@AllArgsConstructor
public enum CustomCommonError implements ICustomError {

    INVALID_REQUEST_FIELD("COMMON-001", "요청 필드 검증에 실패했습니다.", BAD_REQUEST),
    MALFORMED_REQUEST_BODY("COMMON-002", "잘못된 JSON 형식입니다.", BAD_REQUEST),
    INVALID_REQUEST_PARAMETER("COMMON-003", "요청 파라미터 검증에 실패했습니다.", BAD_REQUEST),
    MISSING_REQUEST_PARAMETER("COMMON-004", "필수 요청 파라미터가 없습니다.", BAD_REQUEST),
    REQUEST_VALUE_TYPE_MISMATCH("COMMON-005", "요청 값의 형식이 올바르지 않습니다.", BAD_REQUEST),
    MISSING_REQUEST_PART("COMMON-006", "필수 요청 파트가 없습니다.", BAD_REQUEST),
    UPLOAD_SIZE_EXCEEDED("COMMON-007", "업로드 용량 제한을 초과했습니다.", BAD_REQUEST),
    ENDPOINT_NOT_FOUND("COMMON-008", "요청한 API 엔드포인트를 찾을 수 없습니다.", NOT_FOUND),
    METHOD_NOT_SUPPORTED("COMMON-009", "지원하지 않는 HTTP 메서드입니다.", METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE_REQUESTED("COMMON-010", "지원하지 않는 Content-Type입니다.", UNSUPPORTED_MEDIA_TYPE),
    NOT_ACCEPTABLE_MEDIA_TYPE_REQUESTED("COMMON-011", "지원하지 않는 Accept 타입입니다.", NOT_ACCEPTABLE),
    ACCESS_DENIED("COMMON-012", "요청한 리소스에 접근할 권한이 없습니다.", FORBIDDEN),
    DATA_INTEGRITY_VIOLATED("COMMON-013", "DB 무결성 제약조건이 위반되었습니다.", CONFLICT),
    MISSING_REQUEST_HEADER("COMMON-014", "필수 요청 헤더가 없습니다.", BAD_REQUEST),
    UNEXPECTED_SERVER_ERROR("COMMON-999", "서버 내부 오류가 발생했습니다.", INTERNAL_SERVER_ERROR);

    private final String code;
    private final String description;
    private final HttpStatus statusCode;

}
