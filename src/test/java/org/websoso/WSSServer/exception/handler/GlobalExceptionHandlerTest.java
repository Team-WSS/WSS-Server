package org.websoso.WSSServer.exception.handler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.DUPLICATED_NICKNAME;
import static org.websoso.common.exception.CustomCommonError.DATA_INTEGRITY_VIOLATED;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_FIELD;
import static org.websoso.common.exception.CustomCommonError.MALFORMED_REQUEST_BODY;
import static org.websoso.common.exception.CustomCommonError.MISSING_REQUEST_PARAMETER;
import static org.websoso.common.exception.CustomCommonError.MISSING_REQUEST_PART;
import static org.websoso.common.exception.CustomCommonError.REQUEST_VALUE_TYPE_MISMATCH;
import static org.websoso.common.exception.CustomCommonError.UPLOAD_SIZE_EXCEEDED;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.sql.SQLIntegrityConstraintViolationException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 요청 처리 공통 오류를 {@link GlobalExceptionHandler}가 어떤 코드와 메시지로 응답하는지 검증한다.
 * 특정 도메인 API에 의존하지 않도록 검증 대상 예외만 발생시키는 Controller를 두고 MockMvc로 확인한다.
 */
class GlobalExceptionHandlerTest {

    private static final String NAME_NOT_BLANK = "이름은 비어 있거나, 공백일 수 없습니다.";
    private static final String COUNT_POSITIVE = "개수는 양수여야 합니다.";
    private static final String UNKNOWN_CONSTRAINT_NAME = "UNIQUE_SOME_OTHER_CONSTRAINT";
    private static final long MAX_FILE_SIZE_BYTES = 512 * 1024L;
    private static final long MAX_TOTAL_FILE_SIZE_BYTES = 2621440L;

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @DisplayName("요청 본문 DTO 검증에 실패하면 400 COMMON-001과 실패한 검증 메시지로 응답한다")
    @Test
    void respondsWithCommonInvalidRequestFieldWhenRequestFieldIsInvalid() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\" \",\"count\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_FIELD.getCode()))
                .andExpect(jsonPath("$.message").value(NAME_NOT_BLANK));
    }

    /**
     * 코드는 어느 필드가 실패했는지와 무관하게 고정되고, 메시지만 실패한 검증 애너테이션의 것으로 바뀐다.
     * 필드마다 별도의 공통 코드를 만들지 않는다는 계약을 확인한다.
     */
    @DisplayName("검증에 실패한 필드가 달라져도 코드는 COMMON-001로 같고 메시지만 달라진다")
    @Test
    void keepsCommonCodeAndChangesOnlyMessagePerFailedField() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"이름\",\"count\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_FIELD.getCode()))
                .andExpect(jsonPath("$.message").value(COUNT_POSITIVE));
    }

    @DisplayName("요청 본문을 JSON으로 읽지 못하면 400 COMMON-002와 정의된 메시지로 응답한다")
    @Test
    void respondsWithCommonMalformedRequestBodyWhenBodyIsNotReadable() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MALFORMED_REQUEST_BODY.getCode()))
                .andExpect(jsonPath("$.message").value(MALFORMED_REQUEST_BODY.getDescription()));
    }

    /**
     * 공통 코드 도입이 도메인 예외의 응답 계약을 바꾸지 않는다는 것을 확인한다.
     */
    @DisplayName("도메인 예외는 기존 도메인 코드와 상태 코드로 그대로 응답한다")
    @Test
    void keepsDomainErrorCodeAndStatus() throws Exception {
        mockMvc.perform(post("/test/domain-error")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is(COLLECTION_NOT_FOUND.getStatusCode().value()))
                .andExpect(jsonPath("$.code").value(COLLECTION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(COLLECTION_NOT_FOUND.getDescription()));
    }

    @DisplayName("필수 요청 파라미터가 없으면 400 COMMON-004와 빠진 파라미터 이름으로 응답한다")
    @Test
    void respondsWithCommonMissingRequestParameterWhenRequiredParameterIsAbsent() throws Exception {
        mockMvc.perform(get("/test/parameter"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MISSING_REQUEST_PARAMETER.getCode()))
                .andExpect(jsonPath("$.message").value("필수 요청 파라미터가 없습니다: count"));
    }

    @DisplayName("요청 파라미터를 선언한 타입으로 변환하지 못하면 400 COMMON-005로 응답한다")
    @Test
    void respondsWithCommonTypeMismatchWhenParameterCannotBeConverted() throws Exception {
        mockMvc.perform(get("/test/parameter").queryParam("count", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(REQUEST_VALUE_TYPE_MISMATCH.getCode()))
                .andExpect(jsonPath("$.message").value("요청 값의 형식이 올바르지 않습니다: count"));
    }

    @DisplayName("multipart 요청에 필수 파트가 없으면 400 COMMON-006과 빠진 파트 이름으로 응답한다")
    @Test
    void respondsWithCommonMissingRequestPartWhenRequiredPartIsAbsent() throws Exception {
        mockMvc.perform(multipart("/test/upload"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(MISSING_REQUEST_PART.getCode()))
                .andExpect(jsonPath("$.message").value("필수 요청 파트가 없습니다: image"));
    }

    /**
     * 업로드 용량 초과는 Servlet 컨테이너의 multipart 파서가 던지므로 MockMvc 요청으로는 재현할 수 없다.
     * 예외 발생 지점만 대신하고, 그 예외가 어떤 상태 코드·코드·메시지로 나가는지는 실제 응답으로 확인한다.
     * HTTP 상태는 기존 계약대로 400을 유지한다.
     */
    @DisplayName("파일 하나가 용량 제한을 넘으면 400 COMMON-007과 파일당 제한 메시지로 응답한다")
    @Test
    void respondsWithCommonUploadSizeExceededForSingleFile() throws Exception {
        mockMvc.perform(post("/test/upload-too-large"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(UPLOAD_SIZE_EXCEEDED.getCode()))
                .andExpect(jsonPath("$.message").value("업로드하는 각 파일의 용량이 0.5MB를 초과할 수 없습니다."));
    }

    @DisplayName("전체 요청이 용량 제한을 넘으면 400 COMMON-007과 총 용량 제한 메시지로 응답한다")
    @Test
    void respondsWithCommonUploadSizeExceededForTotalRequest() throws Exception {
        mockMvc.perform(post("/test/upload-total-too-large"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(UPLOAD_SIZE_EXCEEDED.getCode()))
                .andExpect(jsonPath("$.message").value("업로드하는 총 파일의 용량이 2.5MB를 초과할 수 없습니다."));
    }

    /**
     * 도메인이 의미를 아는 제약조건만 도메인 코드로 분류한다는 기존 분기를 유지하는지 확인한다.
     */
    @DisplayName("닉네임 중복 제약조건 위반은 기존 도메인 코드 USER-009로 응답한다")
    @Test
    void keepsDomainCodeForDuplicatedNicknameConstraint() throws Exception {
        mockMvc.perform(post("/test/duplicated-nickname"))
                .andExpect(status().is(DUPLICATED_NICKNAME.getStatusCode().value()))
                .andExpect(jsonPath("$.code").value(DUPLICATED_NICKNAME.getCode()))
                .andExpect(jsonPath("$.message").value(DUPLICATED_NICKNAME.getDescription()));
    }

    /**
     * 분류되지 않은 무결성 위반은 공통 코드로 응답한다.
     * 제약조건 이름과 root cause 메시지는 분류에만 쓰고 응답에는 남기지 않는다.
     */
    @DisplayName("분류되지 않은 DB 무결성 위반은 409 COMMON-013으로 응답하고 제약조건 이름을 노출하지 않는다")
    @Test
    void respondsWithCommonDataIntegrityViolationWithoutLeakingConstraintName() throws Exception {
        mockMvc.perform(post("/test/unknown-constraint"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(DATA_INTEGRITY_VIOLATED.getCode()))
                .andExpect(jsonPath("$.message").value(DATA_INTEGRITY_VIOLATED.getDescription()))
                .andExpect(content().string(not(containsString(UNKNOWN_CONSTRAINT_NAME))));
    }

    record TestRequest(
            @NotBlank(message = NAME_NOT_BLANK) String name,
            @Positive(message = COUNT_POSITIVE) int count
    ) {
    }

    @RestController
    static class TestController {

        @PostMapping("/test/validated")
        ResponseEntity<Void> validated(@Valid @RequestBody TestRequest request) {
            return ResponseEntity.noContent().build();
        }

        @PostMapping("/test/domain-error")
        ResponseEntity<Void> domainError() {
            throw new CustomCollectionException(COLLECTION_NOT_FOUND, "collection with the given id was not found");
        }

        @GetMapping("/test/parameter")
        ResponseEntity<Void> parameter(@RequestParam("count") int count) {
            return ResponseEntity.noContent().build();
        }

        @PostMapping("/test/upload")
        ResponseEntity<Void> upload(@RequestPart("image") MultipartFile image) {
            return ResponseEntity.noContent().build();
        }

        @PostMapping("/test/upload-too-large")
        ResponseEntity<Void> uploadTooLarge() {
            throw new MaxUploadSizeExceededException(MAX_FILE_SIZE_BYTES);
        }

        @PostMapping("/test/upload-total-too-large")
        ResponseEntity<Void> uploadTotalTooLarge() {
            throw new MaxUploadSizeExceededException(MAX_TOTAL_FILE_SIZE_BYTES,
                    new SizeLimitExceededException("request too large", MAX_TOTAL_FILE_SIZE_BYTES,
                            MAX_TOTAL_FILE_SIZE_BYTES));
        }

        @PostMapping("/test/duplicated-nickname")
        ResponseEntity<Void> duplicatedNickname() {
            throw dataIntegrityViolation("UNIQUE_NICKNAME_CONSTRAINT");
        }

        @PostMapping("/test/unknown-constraint")
        ResponseEntity<Void> unknownConstraint() {
            throw dataIntegrityViolation(UNKNOWN_CONSTRAINT_NAME);
        }

        private DataIntegrityViolationException dataIntegrityViolation(String constraintName) {
            return new DataIntegrityViolationException("could not execute statement",
                    new SQLIntegrityConstraintViolationException(
                            "Duplicate entry for constraint " + constraintName));
        }
    }
}
