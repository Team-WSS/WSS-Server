package org.websoso.WSSServer.exception.handler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_FIELD;
import static org.websoso.common.exception.CustomCommonError.MALFORMED_REQUEST_BODY;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.websoso.common.exception.AbstractCustomException;
import org.websoso.common.exception.CustomCommonError;
import org.websoso.common.exception.ErrorResult;
import org.websoso.common.exception.ICustomError;

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
     * 이 핸들러가 없으면 예외가 처리되지 않은 채 ERROR 디스패치로 넘어가고, 그 재요청은
     * SecurityContext가 비어 있어 인증에 실패한다. 그 결과 검증 실패가 401 AUTH-001로 둔갑해
     * 제약 조건에 정의한 메시지가 클라이언트에 전달되지 않는다.
     * RequestBody DTO 검증({@link MethodArgumentNotValidException})과 같은 {@link ErrorResult} 형식으로 응답한다.
     * 공통 코드 부여 대상은 아직 RequestBody DTO 검증뿐이라 코드는 기존대로 HTTP 상태 이름을 쓴다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResult> ConstraintViolationExceptionHandler(ConstraintViolationException e) {
        log.error("[ConstraintViolationException] exception ", e);
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(BAD_REQUEST.getReasonPhrase());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResult(BAD_REQUEST.name(), message));
    }

    /**
     * 필수 요청 파라미터가 없을 때를 처리한다.
     * 처리하지 않으면 {@link ConstraintViolationException}과 같은 경로로 401 AUTH-001이 된다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResult> MissingServletRequestParameterExceptionHandler(
            MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterException] exception ", e);
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResult(BAD_REQUEST.name(),
                        "필수 요청 파라미터가 없습니다: " + e.getParameterName()));
    }

    /**
     * 요청 파라미터나 경로 변수를 선언한 타입으로 변환하지 못할 때를 처리한다.
     * 정의되지 않은 Enum 값과 숫자 자리에 온 문자열이 여기에 해당한다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResult> MethodArgumentTypeMismatchExceptionHandler(
            MethodArgumentTypeMismatchException e) {
        log.error("[MethodArgumentTypeMismatchException] exception ", e);
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResult(BAD_REQUEST.name(),
                        "요청 값의 형식이 올바르지 않습니다: " + e.getName()));
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResult> DataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException] exception ", e);

        String rootCauseMessage = Objects.requireNonNull(e.getRootCause()).getMessage();
        if (rootCauseMessage != null && rootCauseMessage.contains("UNIQUE_NICKNAME_CONSTRAINT")) {
            return ResponseEntity
                    .status(CONFLICT)
                    .body(new ErrorResult(DUPLICATED_NICKNAME.getCode(), DUPLICATED_NICKNAME.getDescription()));
        }

        return ResponseEntity
                .status(CONFLICT)
                .body(new ErrorResult(CONFLICT.name(), "DB 무결성 제약조건이 위반되었습니다."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResult> MaxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException e) {
        log.error("[MaxUploadSizeExceededException] exception ", e);

        String message = (e.getCause() instanceof SizeLimitExceededException)
                ? String.format("업로드하는 총 파일의 용량이 %.1fMB를 초과할 수 없습니다.", MAX_TOTAL_FILE_SIZE_MB)
                : String.format("업로드하는 각 파일의 용량이 %.1fMB를 초과할 수 없습니다.", MAX_FILE_SIZE_MB);

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResult(BAD_REQUEST.name(), message));
    }

    @ExceptionHandler(AbstractCustomException.class)
    public ResponseEntity<ErrorResult> CustomExceptionHandler(AbstractCustomException e) {
        log.error("[{}] exception ", e.getClass().getSimpleName(), e);
        ICustomError iCustomError = e.getICustomError();
        return ResponseEntity
                .status(iCustomError.getStatusCode())
                .body(new ErrorResult(iCustomError.getCode(), iCustomError.getDescription()));
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
     */
    private ResponseEntity<ErrorResult> errorResponse(ICustomError error, String message) {
        return ResponseEntity
                .status(error.getStatusCode())
                .body(new ErrorResult(error.getCode(), message));
    }

}
