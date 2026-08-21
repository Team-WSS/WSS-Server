package org.websoso.WSSServer.auth.controller.auth;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.exception.error.CustomAuthError.ACCESS_TOKEN_EXPIRED;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomAuthError.WRONG_TOKEN_TYPE;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.KAKAO_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.KAKAO_SERVER_ERROR;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;
import static org.websoso.WSSServer.support.docs.RequestPreprocessors.withoutRequestBody;
import static org.websoso.common.exception.CustomCommonError.MALFORMED_REQUEST_BODY;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.websoso.WSSServer.application.AccountApplication;
import org.websoso.WSSServer.auth.application.AuthApplication;
import org.websoso.WSSServer.auth.controller.AuthController;
import org.websoso.WSSServer.auth.controller.dto.LogoutRequest;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.exception.exception.CustomKakaoException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code POST /auth/logout}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 애플리케이션이 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 * 여기서 만든 snippet이 {@code ./gradlew apiDocs}에서 OpenAPI 3 명세로 변환된다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(AuthController.class)
class PostAuthLogoutDocsTest {

    private static final Long USER_ID = 42L;
    private static final LogoutRequest LOGOUT_REQUEST = new LogoutRequest("refresh-token", "device-identifier");
    private static final String TAG = "Auth";
    private static final String SUMMARY = "로그아웃";
    private static final Schema LOGOUT_REQUEST_SCHEMA = Schema.schema("LogoutRequest");

    private static final String DESCRIPTION = String.join("\n",
            "Access Token으로 인증한 사용자의 Refresh Token과 기기 정보를 만료시킵니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 204 No Content — 성공. (응답 본문이 없습니다.)",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(MALFORMED_REQUEST_BODY) + " (응답 본문이 없으며, 본문이 JSON으로 읽히지 않을 때 반환합니다.)",
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            errorLine(KAKAO_REQUEST_FAILED) + " (카카오 사용자의 카카오 로그아웃 요청이 실패했을 때 반환합니다.)",
            errorLine(KAKAO_SERVER_ERROR) + " (카카오 로그아웃 요청에 카카오 서버가 5xx로 응답했을 때 반환합니다.)",
            "",
            "401은 상태 코드만으로 원인을 구분할 수 없습니다. 401 응답의 Examples에서 코드별 본문을 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthApplication authApplication;

    @MockBean
    private AppleService appleService;

    @MockBean
    private AccountApplication accountApplication;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("로그아웃 성공(204, 본문 없음) 응답을 문서화한다")
    @Test
    void documentLogoutSuccess() throws Exception {
        mockMvc.perform(logoutRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("auth-logout",
                        resource(documentedLogout().build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentLogoutWithExpiredAccessToken() throws Exception {
        mockMvc.perform(logoutRequest().with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("auth-logout-access-token-expired",
                        withoutRequestBody(),
                        resource(logoutError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentLogoutWithInvalidToken() throws Exception {
        mockMvc.perform(logoutRequest())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("auth-logout-invalid-token",
                        withoutRequestBody(),
                        resource(logoutError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentLogoutWithWrongTokenType() throws Exception {
        mockMvc.perform(logoutRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("auth-logout-wrong-token-type",
                        withoutRequestBody(),
                        resource(logoutError().build())));
    }

    @DisplayName("본문 JSON 형식이 잘못된 요청의 400 COMMON-002 응답을 문서화한다")
    @Test
    void documentLogoutWithMalformedJson() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content("{\"refreshToken\":")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(MALFORMED_REQUEST_BODY.getCode()))
                .andExpect(jsonPath("$.message").value(MALFORMED_REQUEST_BODY.getDescription()))
                .andDo(document("auth-logout-malformed-json",
                        withoutRequestBody(),
                        resource(logoutError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentLogoutWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(logoutRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("auth-logout-user-not-found",
                        withoutRequestBody(),
                        resource(logoutError().build())));
    }

    @DisplayName("카카오 로그아웃 요청이 실패한 경우의 502 KAKAO-003 응답을 문서화한다")
    @Test
    void documentLogoutWithKakaoRequestFailure() throws Exception {
        willThrow(new CustomKakaoException(KAKAO_REQUEST_FAILED, "kakao request failed during logout"))
                .given(authApplication).logout(any(User.class), eq(LOGOUT_REQUEST));

        mockMvc.perform(logoutRequest().with(accessToken(USER_ID)))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(KAKAO_REQUEST_FAILED.getCode()))
                .andExpect(jsonPath("$.message").value(KAKAO_REQUEST_FAILED.getDescription()))
                .andDo(document("auth-logout-kakao-request-failed",
                        withoutRequestBody(),
                        resource(logoutError().build())));
    }

    @DisplayName("카카오 서버 오류인 경우의 503 KAKAO-002 응답을 문서화한다")
    @Test
    void documentLogoutWithKakaoServerError() throws Exception {
        willThrow(new CustomKakaoException(KAKAO_SERVER_ERROR, "kakao server error during logout"))
                .given(authApplication).logout(any(User.class), eq(LOGOUT_REQUEST));

        mockMvc.perform(logoutRequest().with(accessToken(USER_ID)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(KAKAO_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(KAKAO_SERVER_ERROR.getDescription()))
                .andDo(document("auth-logout-kakao-server-error",
                        withoutRequestBody(),
                        resource(logoutError().build())));
    }

    private ResourceSnippetParametersBuilder documentedLogout() {
        return logout()
                .requestSchema(LOGOUT_REQUEST_SCHEMA)
                .requestFields(logoutRequestFields());
    }

    private ResourceSnippetParametersBuilder logoutError() {
        return logout()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder logout() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION);
    }

    private List<FieldDescriptor> logoutRequestFields() {
        return List.of(
                fieldWithPath("refreshToken").type(STRING).description("만료시킬 Refresh Token"),
                fieldWithPath("deviceIdentifier").type(STRING).description("로그아웃할 기기 식별자"));
    }

    private MockHttpServletRequestBuilder logoutRequest() throws Exception {
        return post("/auth/logout")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LOGOUT_REQUEST));
    }
}
