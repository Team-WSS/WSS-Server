package org.websoso.WSSServer.exception.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_FIELD;
import static org.websoso.common.exception.CustomCommonError.MALFORMED_REQUEST_BODY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 요청 처리 공통 오류를 {@link GlobalExceptionHandler}가 어떤 코드와 메시지로 응답하는지 검증한다.
 * 특정 도메인 API에 의존하지 않도록 검증 대상 예외만 발생시키는 Controller를 두고 MockMvc로 확인한다.
 */
class GlobalExceptionHandlerTest {

    private static final String NAME_NOT_BLANK = "이름은 비어 있거나, 공백일 수 없습니다.";
    private static final String COUNT_POSITIVE = "개수는 양수여야 합니다.";

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
    }
}
