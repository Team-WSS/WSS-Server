package org.websoso.WSSServer.exception.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;
import static org.websoso.common.exception.CustomCommonError.DATA_INTEGRITY_VIOLATED;
import static org.websoso.common.exception.CustomCommonError.ENDPOINT_NOT_FOUND;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_FIELD;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_PARAMETER;
import static org.websoso.common.exception.CustomCommonError.MALFORMED_REQUEST_BODY;
import static org.websoso.common.exception.CustomCommonError.METHOD_NOT_SUPPORTED;
import static org.websoso.common.exception.CustomCommonError.MISSING_REQUEST_HEADER;
import static org.websoso.common.exception.CustomCommonError.MISSING_REQUEST_PARAMETER;
import static org.websoso.common.exception.CustomCommonError.MISSING_REQUEST_PART;
import static org.websoso.common.exception.CustomCommonError.NOT_ACCEPTABLE_MEDIA_TYPE_REQUESTED;
import static org.websoso.common.exception.CustomCommonError.REQUEST_VALUE_TYPE_MISMATCH;
import static org.websoso.common.exception.CustomCommonError.UNEXPECTED_SERVER_ERROR;
import static org.websoso.common.exception.CustomCommonError.UNSUPPORTED_MEDIA_TYPE_REQUESTED;
import static org.websoso.common.exception.CustomCommonError.UPLOAD_SIZE_EXCEEDED;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.websoso.common.exception.AbstractCustomException;
import org.websoso.common.exception.CustomCommonError;
import org.websoso.common.exception.ErrorResult;
import org.websoso.common.exception.ICustomError;

/**
 * 모든 오류 응답을 {@link ErrorResult} 하나의 형식으로 맞추는 전역 예외 처리기.
 *
 * <p>여기서 예외를 잡지 않으면 DispatcherServlet이 컨테이너의 ERROR 디스패치로 넘긴다.
 * ERROR 디스패치는 {@code /error}에 대한 새 요청이고 그 요청은 SecurityContext가 비어 있어 인증에 실패하므로,
 * 원래 400·404·405·406·415·500이어야 할 응답이 401 AUTH-001로 바뀐다.
 * 프레임워크가 던지는 요청·프로토콜 예외를 빠짐없이 잡아야 하는 이유가 이것이다.
 *
 * <p>코드는 항상 {@link ICustomError} 정의에서 가져온다. HTTP 상태 이름은 원인을 구분하는 서비스 에러 코드가
 * 아니므로 {@code code}로 쓰지 않는다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final double MAX_TOTAL_FILE_SIZE_MB = 2.5;
    private static final double MAX_FILE_SIZE_MB = 0.5;

    /**
     * {@code @Valid @RequestBody} DTO의 Bean Validation 실패를 처리한다.
     * 원인이 도메인이 아니라 요청 형식이므로 코드는 공통 {@link CustomCommonError#INVALID_REQUEST_FIELD}로 고정하고,
     * 메시지만 실패한 검증 애너테이션의 것으로 대체해 클라이언트가 어느 필드가 잘못됐는지 알 수 있게 한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("[MethodArgumentNotValidException] exception ", e);
        String message = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(ObjectError::getDefaultMessage)
                .orElse(INVALID_REQUEST_FIELD.getDescription());
        return errorResponse(INVALID_REQUEST_FIELD, message);
    }

    /**
     * {@code @Validated}가 붙은 Controller의 PathVariable, RequestParam 검증 실패를 처리한다.
     * RequestBody DTO 검증({@link MethodArgumentNotValidException})과 같은 형식으로 응답하되,
     * 실패 지점이 본문 필드가 아니라 요청 파라미터이므로 코드는 {@link CustomCommonError#INVALID_REQUEST_PARAMETER}다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResult> ConstraintViolationExceptionHandler(ConstraintViolationException e) {
        log.error("[ConstraintViolationException] exception ", e);
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(INVALID_REQUEST_PARAMETER.getDescription());
        return errorResponse(INVALID_REQUEST_PARAMETER, message);
    }

    /**
     * Spring 6.1의 Controller 메서드 파라미터 검증 실패를 처리한다.
     * 클래스에 {@code @Validated}가 없으면 {@code @Positive} 같은 파라미터 제약은
     * AOP가 아니라 {@code RequestMappingHandlerAdapter}가 직접 검증하고
     * {@link ConstraintViolationException} 대신 이 예외를 던진다.
     * 클라이언트 입장에서는 같은 원인이므로 {@link ConstraintViolationException}과 같은 코드로 응답한다.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResult> HandlerMethodValidationExceptionHandler(HandlerMethodValidationException e) {
        log.error("[HandlerMethodValidationException] exception ", e);
        String message = e.getAllErrors().stream()
                .findFirst()
                .map(MessageSourceResolvable::getDefaultMessage)
                .orElse(INVALID_REQUEST_PARAMETER.getDescription());
        return errorResponse(INVALID_REQUEST_PARAMETER, message);
    }

    /**
     * 필수 요청 파라미터가 없을 때를 처리한다.
     * 어떤 파라미터가 빠졌는지는 요청마다 다르므로 이름만 메시지에 덧붙인다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResult> MissingServletRequestParameterExceptionHandler(
            MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterException] exception ", e);
        return errorResponse(MISSING_REQUEST_PARAMETER,
                "필수 요청 파라미터가 없습니다: " + e.getParameterName());
    }

    /**
     * 필수 요청 헤더가 없을 때를 처리한다.
     * 어떤 헤더가 빠졌는지는 요청마다 다르므로 이름만 메시지에 덧붙인다.
     *
     * <p>인증 헤더 누락은 여기까지 오지 않는다. 인증이 필요한 엔드포인트는 Controller에 도달하기 전에
     * {@code JwtAuthenticationFilter}가 401 {@code AUTH-001}로 끝내므로 기존 인증 계약이 그대로 유지된다.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResult> MissingRequestHeaderExceptionHandler(MissingRequestHeaderException e) {
        log.error("[MissingRequestHeaderException] exception ", e);
        return errorResponse(MISSING_REQUEST_HEADER,
                "필수 요청 헤더가 없습니다: " + e.getHeaderName());
    }

    /**
     * 요청 파라미터나 경로 변수를 선언한 타입으로 변환하지 못할 때를 처리한다.
     * 정의되지 않은 Enum 값과 숫자 자리에 온 문자열이 여기에 해당한다.
     * 변환에 실패한 값 자체는 되돌려주지 않고 파라미터 이름만 알려준다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResult> MethodArgumentTypeMismatchExceptionHandler(
            MethodArgumentTypeMismatchException e) {
        log.error("[MethodArgumentTypeMismatchException] exception ", e);
        return errorResponse(REQUEST_VALUE_TYPE_MISMATCH,
                "요청 값의 형식이 올바르지 않습니다: " + e.getName());
    }

    /**
     * multipart 요청에 필수 파트가 없을 때를 처리한다.
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResult> MissingServletRequestPartExceptionHandler(
            MissingServletRequestPartException e) {
        log.error("[MissingServletRequestPartException] exception ", e);
        return errorResponse(MISSING_REQUEST_PART,
                "필수 요청 파트가 없습니다: " + e.getRequestPartName());
    }

    /**
     * 요청 본문을 JSON으로 읽지 못했을 때를 처리한다.
     * 본문을 읽지 못했으므로 어느 필드가 문제인지 알 수 없고, 코드와 메시지 모두
     * {@link CustomCommonError#MALFORMED_REQUEST_BODY} 정의를 그대로 사용한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResult> HttpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] exception ", e);
        return errorResponse(MALFORMED_REQUEST_BODY);
    }

    /**
     * 매핑된 엔드포인트가 없는 요청을 처리한다.
     * 정적 리소스 핸들러가 매핑을 가져가는 경우 {@link NoResourceFoundException}이,
     * 그렇지 않은 구성에서는 {@link NoHandlerFoundException}이 발생하므로 둘 다 같은 코드로 응답한다.
     *
     * <p>도메인 리소스가 없는 404({@code NOVEL-001} 등)와는 다른 오류다.
     * 클라이언트가 요청 경로 자체를 고쳐야 하는지 요청한 자원만 없는 것인지 코드로 구분할 수 있어야 한다.
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResult> NoEndpointFoundExceptionHandler(Exception e) {
        log.warn("[{}] exception ", e.getClass().getSimpleName(), e);
        return errorResponse(ENDPOINT_NOT_FOUND);
    }

    /**
     * 엔드포인트는 있으나 요청한 HTTP 메서드를 지원하지 않을 때를 처리한다.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResult> HttpRequestMethodNotSupportedExceptionHandler(
            HttpRequestMethodNotSupportedException e) {
        log.warn("[HttpRequestMethodNotSupportedException] exception ", e);
        return errorResponse(METHOD_NOT_SUPPORTED);
    }

    /**
     * 요청 본문의 Content-Type을 처리할 수 없을 때를 처리한다.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResult> HttpMediaTypeNotSupportedExceptionHandler(
            HttpMediaTypeNotSupportedException e) {
        log.warn("[HttpMediaTypeNotSupportedException] exception ", e);
        return errorResponse(UNSUPPORTED_MEDIA_TYPE_REQUESTED);
    }

    /**
     * 요청한 Accept 타입으로 응답을 만들 수 없을 때를 처리한다.
     * 이 오류만은 오류 응답 자체도 협상 대상이므로, {@link #errorResponse}가 Content-Type을
     * JSON으로 고정하지 않으면 응답을 쓰는 과정에서 같은 예외가 다시 발생한다.
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResult> HttpMediaTypeNotAcceptableExceptionHandler(
            HttpMediaTypeNotAcceptableException e) {
        log.warn("[HttpMediaTypeNotAcceptableException] exception ", e);
        return errorResponse(NOT_ACCEPTABLE_MEDIA_TYPE_REQUESTED);
    }

    /**
     * DB 무결성 제약조건 위반을 처리한다.
     * 도메인이 의미를 아는 제약조건만 도메인 코드로 분류하고, 나머지는 공통 코드로 응답한다.
     * 제약조건 이름과 root cause 메시지는 분류에만 쓰고 응답에는 내보내지 않는다.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResult> DataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException] exception ", e);

        Throwable rootCause = e.getRootCause();
        String rootCauseMessage = rootCause == null ? null : rootCause.getMessage();
        if (rootCauseMessage != null && rootCauseMessage.contains("UNIQUE_NICKNAME_CONSTRAINT")) {
            return errorResponse(DUPLICATED_NICKNAME);
        }

        return errorResponse(DATA_INTEGRITY_VIOLATED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResult> MaxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException e) {
        log.error("[MaxUploadSizeExceededException] exception ", e);

        String message = (e.getCause() instanceof SizeLimitExceededException)
                ? String.format("업로드하는 총 파일의 용량이 %.1fMB를 초과할 수 없습니다.", MAX_TOTAL_FILE_SIZE_MB)
                : String.format("업로드하는 각 파일의 용량이 %.1fMB를 초과할 수 없습니다.", MAX_FILE_SIZE_MB);

        return errorResponse(UPLOAD_SIZE_EXCEEDED, message);
    }

    @ExceptionHandler(AbstractCustomException.class)
    public ResponseEntity<ErrorResult> CustomExceptionHandler(AbstractCustomException e) {
        log.error("[{}] exception ", e.getClass().getSimpleName(), e);
        return errorResponse(e.getICustomError());
    }

    /**
     * 인가 실패는 여기서 응답을 만들지 않고 그대로 다시 던진다.
     * {@code @PreAuthorize} 위반은 Controller 호출 중에 발생하므로 아래 {@link #UnhandledExceptionHandler(Exception)}
     * fallback이 먼저 잡으면 403이어야 할 응답이 500이 된다. 원래 예외를 그대로 던지면
     * DispatcherServlet이 예외를 다시 밖으로 흘려보내고, Security의 {@code ExceptionTranslationFilter}가
     * 익명 사용자 여부에 따라 401 또는 403 {@code COMMON-012}로 응답한다. 인가 정책 판단을 Security에 남겨 둔다.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void AccessDeniedExceptionHandler(AccessDeniedException e) throws AccessDeniedException {
        throw e;
    }

    /**
     * 위에서 처리하지 못한 모든 예외의 최종 fallback.
     * 이 핸들러가 없으면 예상하지 못한 예외가 ERROR 디스패치로 넘어가 500이 401 AUTH-001로 바뀐다.
     *
     * <p>응답 메시지는 정의된 고정 문자열만 쓴다. 원본 예외 메시지에는 SQL, 제약조건 이름, 내부 경로가
     * 섞일 수 있어 그대로 내보내면 안 된다. 원인 파악에 필요한 전체 예외는 로그에만 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> UnhandledExceptionHandler(Exception e) {
        log.error("[{}] unhandled exception ", e.getClass().getSimpleName(), e);
        return errorResponse(UNEXPECTED_SERVER_ERROR);
    }

    /**
     * {@link ICustomError} 정의 하나로 응답을 만든다. 상태 코드, 코드, 메시지가 모두 정의에서 정해지는 오류에 쓴다.
     */
    private ResponseEntity<ErrorResult> errorResponse(ICustomError error) {
        return errorResponse(error, error.getDescription());
    }

    /**
     * {@link ICustomError} 정의의 상태 코드와 코드에 동적 메시지를 조합해 응답을 만든다.
     * 코드는 정의에서만 가져오므로 같은 원인의 오류가 엔드포인트마다 다른 코드로 나가지 않고,
     * 요청마다 달라지는 실패 원인은 메시지로만 전달된다.
     *
     * <p>Content-Type을 JSON으로 고정한다. 지정하지 않으면 응답을 쓸 때 Accept 헤더로 다시 협상하게 되고,
     * JSON을 받지 못하는 Accept로 들어온 요청은 오류 응답조차 만들지 못한다.
     */
    private ResponseEntity<ErrorResult> errorResponse(ICustomError error, String message) {
        return ResponseEntity
                .status(error.getStatusCode())
                .contentType(APPLICATION_JSON)
                .body(new ErrorResult(error.getCode(), message));
    }

}
