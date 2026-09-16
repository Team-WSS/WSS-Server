package org.websoso.WSSServer.exception.handler;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;
import static org.websoso.WSSServer.exception.handler.CommonErrorTestController.REQUIRED_HEADER_NAME;
import static org.websoso.WSSServer.exception.handler.CommonErrorTestController.UNEXPECTED_CAUSE_MESSAGE;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.common.exception.CustomCommonError.ACCESS_DENIED;
import static org.websoso.common.exception.CustomCommonError.ENDPOINT_NOT_FOUND;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_PARAMETER;
import static org.websoso.common.exception.CustomCommonError.METHOD_NOT_SUPPORTED;
import static org.websoso.common.exception.CustomCommonError.MISSING_REQUEST_HEADER;
import static org.websoso.common.exception.CustomCommonError.NOT_ACCEPTABLE_MEDIA_TYPE_REQUESTED;
import static org.websoso.common.exception.CustomCommonError.UNEXPECTED_SERVER_ERROR;
import static org.websoso.common.exception.CustomCommonError.UNSUPPORTED_MEDIA_TYPE_REQUESTED;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * 도메인과 무관하게 발생하는 공통 오류가 실제 요청에서 어떤 상태 코드와 {@code COMMON-*} 코드로 응답하는지 검증한다.
 *
 * <p>Security 필터 체인을 그대로 통과하는 슬라이스에서 확인한다. 이 오류들의 핵심은 처리되지 않은 예외가
 * ERROR 디스패치로 넘어가 401 AUTH-001로 둔갑하지 않는 것이고, 그것은 필터 체인 없이 확인할 수 없다.
 */
@AuthenticatedControllerTest(CommonErrorTestController.class)
class CommonErrorResponseTest {

    private static final Long USER_ID = 42L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("매핑된 엔드포인트가 없으면 404 COMMON-008로 응답한다")
    @Test
    void respondsWithEndpointNotFoundWhenNoEndpointIsMapped() throws Exception {
        mockMvc.perform(get("/test/common-errors/no-such-endpoint").with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ENDPOINT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ENDPOINT_NOT_FOUND.getDescription()));
    }

    /**
     * 엔드포인트가 없는 404와 도메인 리소스가 없는 404는 상태 코드가 같고 코드가 다르다.
     * 클라이언트가 경로를 고쳐야 하는지 자원만 없는 것인지 구분할 수 있어야 한다.
     */
    @DisplayName("도메인 리소스 404는 엔드포인트 404와 다른 도메인 코드로 응답한다")
    @Test
    void distinguishesDomainNotFoundFromEndpointNotFound() throws Exception {
        mockMvc.perform(get("/test/common-errors/domain-error").with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(COLLECTION_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(COLLECTION_NOT_FOUND.getDescription()));
    }

    @DisplayName("지원하지 않는 HTTP 메서드로 요청하면 405 COMMON-009로 응답한다")
    @Test
    void respondsWithMethodNotSupportedWhenMethodIsNotAllowed() throws Exception {
        mockMvc.perform(get("/test/common-errors/json-body").with(accessToken(USER_ID)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(METHOD_NOT_SUPPORTED.getCode()))
                .andExpect(jsonPath("$.message").value(METHOD_NOT_SUPPORTED.getDescription()));
    }

    @DisplayName("지원하지 않는 Content-Type으로 요청하면 415 COMMON-010으로 응답한다")
    @Test
    void respondsWithUnsupportedMediaTypeWhenContentTypeIsNotSupported() throws Exception {
        mockMvc.perform(post("/test/common-errors/json-body")
                        .contentType(TEXT_PLAIN)
                        .content("plain text")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(UNSUPPORTED_MEDIA_TYPE_REQUESTED.getCode()))
                .andExpect(jsonPath("$.message").value(UNSUPPORTED_MEDIA_TYPE_REQUESTED.getDescription()));
    }

    /**
     * 오류 응답 본문 자체가 Accept 협상 대상이 되면 406을 응답할 방법이 없다.
     * 핸들러가 Content-Type을 JSON으로 고정하므로 JSON을 받지 않겠다는 요청에도 본문이 내려간다.
     */
    @DisplayName("지원하지 않는 Accept 타입으로 요청하면 406 COMMON-011을 JSON 본문으로 응답한다")
    @Test
    void respondsWithNotAcceptableWhenAcceptTypeIsNotSupported() throws Exception {
        mockMvc.perform(get("/test/common-errors/json-only")
                        .header(ACCEPT, APPLICATION_XML_VALUE)
                        .with(accessToken(USER_ID)))
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(NOT_ACCEPTABLE_MEDIA_TYPE_REQUESTED.getCode()))
                .andExpect(jsonPath("$.message").value(NOT_ACCEPTABLE_MEDIA_TYPE_REQUESTED.getDescription()));
    }

    /**
     * 인증은 됐지만 권한이 없는 요청. 예외를 전역 처리기가 가로채지 않고 Security로 흘려보내
     * {@code CustomAccessDeniedHandler}가 응답을 만든다.
     */
    @DisplayName("인증된 사용자가 권한 없는 API를 호출하면 403 COMMON-012로 응답한다")
    @Test
    void respondsWithAccessDeniedWhenAuthorizationFails() throws Exception {
        mockMvc.perform(get("/test/common-errors/admin-only").with(accessToken(USER_ID)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_DENIED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_DENIED.getDescription()));
    }

    @DisplayName("예상하지 못한 예외는 500 COMMON-999로 응답하고 원본 예외 내용을 노출하지 않는다")
    @Test
    void respondsWithUnexpectedServerErrorWithoutLeakingInternals() throws Exception {
        mockMvc.perform(get("/test/common-errors/unexpected").with(accessToken(USER_ID)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(UNEXPECTED_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(UNEXPECTED_SERVER_ERROR.getDescription()))
                .andExpect(content().string(not(containsString(UNEXPECTED_CAUSE_MESSAGE))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    /**
     * {@code @Validated}가 없는 Controller의 파라미터 제약은 {@code HandlerMethodValidationException}으로 실패한다.
     * 처리하지 않으면 검증 실패가 401 AUTH-001이 되므로 같은 요청으로 코드와 메시지를 함께 확인한다.
     */
    @DisplayName("경로 변수 제약을 위반하면 400 COMMON-003과 제약 메시지로 응답한다")
    @Test
    void respondsWithInvalidRequestParameterWhenPathVariableViolatesConstraint() throws Exception {
        mockMvc.perform(get("/test/common-errors/positive/{id}", 0).with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_PARAMETER.getCode()));
    }

    /**
     * 필수 헤더 누락은 클라이언트가 요청을 고치면 되는 예측 가능한 실패다.
     * 전용 매핑이 없으면 fallback이 잡아 500 COMMON-999가 되므로 코드와 상태를 함께 확인한다.
     */
    @DisplayName("필수 요청 헤더가 없으면 400 COMMON-014와 헤더 이름으로 응답한다")
    @Test
    void respondsWithMissingRequestHeaderWhenRequiredHeaderIsAbsent() throws Exception {
        mockMvc.perform(get("/test/common-errors/required-header").with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(MISSING_REQUEST_HEADER.getCode()))
                .andExpect(jsonPath("$.message").value(containsString(REQUIRED_HEADER_NAME)));
    }

    @DisplayName("필수 요청 헤더 누락은 500 COMMON-999로 응답하지 않는다")
    @Test
    void doesNotFallBackToUnexpectedServerErrorForMissingRequiredHeader() throws Exception {
        mockMvc.perform(get("/test/common-errors/required-header").with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(not(UNEXPECTED_SERVER_ERROR.getCode())));
    }

    @DisplayName("필수 요청 헤더를 담아 요청하면 정상 응답한다")
    @Test
    void respondsSuccessfullyWhenRequiredHeaderIsPresent() throws Exception {
        mockMvc.perform(get("/test/common-errors/required-header")
                        .header(REQUIRED_HEADER_NAME, "value")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isNoContent());
    }

    /**
     * 인증 헤더 자체가 없는 요청은 Controller에 도달하지 않는다.
     * 필수 헤더 누락을 공통 코드로 처리해도 인증 계약은 그대로 401 AUTH-001이어야 한다.
     */
    @DisplayName("인증 정보 없이 필수 헤더 엔드포인트를 호출하면 COMMON-014가 아니라 401 AUTH-001로 응답한다")
    @Test
    void keepsAuthErrorWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/test/common-errors/required-header"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()));
    }

    /**
     * 공통 오류 처리를 추가해도 인증 실패는 그대로 AUTH-001이어야 한다.
     * 인증되지 않은 요청은 Controller에 도달하기 전에 끝나므로 공통 코드로 바뀌지 않는다.
     */
    @DisplayName("인증 정보가 없는 요청은 공통 코드가 아니라 401 AUTH-001로 응답한다")
    @Test
    void keepsAuthErrorForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/test/common-errors/json-only"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()));
    }

    /**
     * 인증되지 않은 요청이 매핑되지 않은 경로로 들어오면 엔드포인트 존재 여부를 알려주지 않고 인증 오류로 끝난다.
     * 인가 정책이 공통 오류 처리보다 먼저 적용된다는 기존 계약을 유지한다.
     */
    @DisplayName("인증 정보가 없으면 매핑되지 않은 경로도 401 AUTH-001로 응답한다")
    @Test
    void keepsAuthErrorForUnauthenticatedRequestToUnmappedPath() throws Exception {
        mockMvc.perform(get("/test/common-errors/no-such-endpoint"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()));
    }
}
