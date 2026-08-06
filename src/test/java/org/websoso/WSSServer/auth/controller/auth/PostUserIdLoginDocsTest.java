package org.websoso.WSSServer.auth.controller.auth;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;
import static org.websoso.WSSServer.support.docs.RequestPreprocessors.withoutRequestBody;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import java.util.List;
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
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.dto.user.LoginResponse;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code POST /users/login}의 실제 응답을 문서화하는 REST Docs 테스트.
 * 소셜 로그인 없이 사용자 ID만으로 Access Token을 발급하는 Deprecated API를,
 * 개발 중 Swagger Authorize에 넣을 토큰을 얻는 보조 수단으로 명세에 남긴다.
 *
 * <p>인증이 필요 없는 경로이므로 요청에 {@code Authorization} 헤더를 넣지 않는다.
 * restdocs-api-spec은 요청 헤더의 Bearer 토큰을 보고 security requirement를 만들기 때문에,
 * 토큰을 함께 보내면 인증이 필요 없는 API에 Authorize 요구가 붙는다.
 *
 * <p>응답 예시의 Access Token은 문서용 placeholder({@link #ACCESS_TOKEN})다.
 * 토큰 발급 자체는 {@code AuthApplication}의 책임이고, 이 테스트가 검증하는 계약은
 * 성공 시 {@code Authorization} 필드에 Access Token이 담긴다는 응답 형식이다.
 *
 * <p>요청 본문은 JSON 객체가 아니라 숫자 하나이므로 {@code requestFields}로 서술하지 않는다.
 * restdocs-api-spec의 스키마 생성기는 루트를 항상 object로 만들고 루트 배열만 되돌리기 때문에,
 * 생성 명세의 요청 스키마는 실제 계약과 달리 빈 object로 남는다. 루트 스칼라를 서술하려고
 * {@code fieldWithPath("[]")}나 {@code fieldWithPath("")}를 쓰면 REST Docs payload 검증에서 예외가 난다.
 * 자세한 근거와 대안은 {@code docs/api-docs.md}의 "알려진 제약" 절에 있다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(AuthController.class)
class PostUserIdLoginDocsTest {

    private static final Long USER_ID = 42L;
    private static final String ACCESS_TOKEN = "<Access Token>";
    private static final String TAG = "Auth";
    private static final String SUMMARY = "[개발용] 사용자 ID로 Access Token 발급";
    private static final Schema LOGIN_RESPONSE_SCHEMA = Schema.schema("LoginResponse");

    private static final String DESCRIPTION = String.join("\n",
            "개발 중 Access Token을 얻기 위한 보조 API 입니다. 클라이언트 로직에서 사용하면 안됩니다.",
            "",
            "요청 본문에 DB에 실제로 있는 사용자 ID를 숫자만 담아 보냅니다.",
            "응답의 Authorization 값을 상단 Authorize에 입력하면 인증이 필요한 API를 호출할 수 있습니다.",
            "Access Token만 발급하고 Refresh Token은 발급하지 않습니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공. (Authorization에 Access Token이 담긴다.)",
            errorLine(USER_NOT_FOUND) + " (요청한 ID의 사용자가 DB에 없을 때 반환합니다.)");

    @Autowired
    private MockMvc mockMvc;

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

    @DisplayName("사용자 ID로 Access Token을 발급받는 200 응답을 문서화한다")
    @Test
    void documentLoginSuccess() throws Exception {
        given(authApplication.login(USER_ID)).willReturn(LoginResponse.of(ACCESS_TOKEN));

        mockMvc.perform(loginRequest())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.Authorization").value(ACCESS_TOKEN))
                .andDo(document("users-login",
                        resource(devLogin()
                                .responseSchema(LOGIN_RESPONSE_SCHEMA)
                                .responseFields(loginResponseFields())
                                .build())));
    }

    @DisplayName("존재하지 않는 사용자 ID 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentLoginWithUnknownUser() throws Exception {
        given(authApplication.login(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(loginRequest())
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("users-login-user-not-found",
                        withoutRequestBody(),
                        resource(devLogin()
                                .responseSchema(ERROR_RESULT_SCHEMA)
                                .responseFields(errorResultFields())
                                .build())));
    }

    /**
     * {@code deprecated(true)}는 같은 경로·메서드의 모든 문서가 지정해야 명세에 남는다.
     * 생성기가 문서 하나라도 빠지면 operation의 {@code deprecated}를 생략한다.
     */
    private ResourceSnippetParametersBuilder devLogin() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION)
                .deprecated(true);
    }

    private List<FieldDescriptor> loginResponseFields() {
        return List.of(fieldWithPath("Authorization").type(STRING)
                .description("발급된 Access Token. 상단 Authorize에 그대로 입력한다."));
    }

    private MockHttpServletRequestBuilder loginRequest() {
        return post("/users/login")
                .contentType(APPLICATION_JSON)
                .content(String.valueOf(USER_ID));
    }
}
