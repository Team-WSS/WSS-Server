package org.websoso.WSSServer.notification.controller.novelnotification;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.exception.error.CustomAuthError.ACCESS_TOKEN_EXPIRED;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomAuthError.WRONG_TOKEN_TYPE;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_ID_POSITIVE;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
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
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.notification.application.NovelNotificationApplication;
import org.websoso.WSSServer.notification.controller.NovelNotificationController;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationResponse;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code GET /novels/{novelId}/notification}의 실제 응답을 문서화하는 REST Docs 테스트.
 * 작품 상세에서 완결·휴재 복귀 알림의 현재 상태를 조회하는 API다.
 *
 * <p>경로 변수 검증 실패(400)는 {@code ConstraintViolationException} 경로를 탄다.
 * 이 예외가 처리되지 않으면 ERROR 디스패치의 재요청이 인증에 실패해 401 AUTH-001로 둔갑하므로,
 * 검증 메시지가 그대로 전달되는지 이 테스트가 함께 확인한다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(NovelNotificationController.class)
class GetNovelNotificationDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long NOVEL_ID = 1L;
    private static final Long INVALID_NOVEL_ID = 0L;
    private static final String TAG = "Novel Notification";
    private static final String SUMMARY = "작품 알림 설정 조회";
    private static final Schema RESPONSE_SCHEMA = Schema.schema("NovelNotificationResponse");

    private static final String DESCRIPTION = String.join("\n",
            "작품 상세에서 사용자가 등록한 완결·휴재 복귀 알림의 현재 상태를 조회합니다.",
            "",
            "두 값은 서로 독립적이며, 등록하지 않은 알림은 false로 내려갑니다.",
            "알림이 이미 발송된 경우에도 false로 내려갑니다. 다시 켜면 새 등록으로 처리되어 등록일이 갱신됩니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), NOVEL_ID_POSITIVE),
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            errorLine(NOVEL_NOT_FOUND),
            "",
            "401은 상태 코드만으로 원인을 구분할 수 없습니다. 401 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private NovelNotificationApplication novelNotificationApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("작품 알림 설정을 조회하는 200 응답을 문서화한다")
    @Test
    void documentGetSettingsSuccess() throws Exception {
        given(novelNotificationApplication.getSettings(any(User.class), eq(NOVEL_ID)))
                .willReturn(new NovelNotificationResponse(true, false));

        mockMvc.perform(settingsRequest(NOVEL_ID).with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.isCompletionNotificationEnabled").value(true))
                .andExpect(jsonPath("$.isHiatusReturnNotificationEnabled").value(false))
                .andDo(document("novel-notification",
                        resource(settings()
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseFields(settingsResponseFields())
                                .build())));
    }

    @DisplayName("양수가 아닌 작품 ID 요청의 400 응답을 문서화한다")
    @Test
    void documentGetSettingsWithNonPositiveNovelId() throws Exception {
        mockMvc.perform(settingsRequest(INVALID_NOVEL_ID).with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NOVEL_ID_POSITIVE))
                .andDo(document("novel-notification-novel-id-not-positive",
                        resource(settingsError().build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentGetSettingsWithExpiredAccessToken() throws Exception {
        mockMvc.perform(settingsRequest(NOVEL_ID).with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("novel-notification-access-token-expired",
                        resource(settingsError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentGetSettingsWithInvalidToken() throws Exception {
        mockMvc.perform(settingsRequest(NOVEL_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("novel-notification-invalid-token",
                        resource(settingsError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentGetSettingsWithWrongTokenType() throws Exception {
        mockMvc.perform(settingsRequest(NOVEL_ID).with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("novel-notification-wrong-token-type",
                        resource(settingsError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentGetSettingsWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(settingsRequest(NOVEL_ID).with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("novel-notification-user-not-found",
                        resource(settingsError().build())));
    }

    @DisplayName("존재하지 않는 작품 요청의 404 NOVEL-001 응답을 문서화한다")
    @Test
    void documentGetSettingsWithUnknownNovel() throws Exception {
        given(novelNotificationApplication.getSettings(any(User.class), eq(NOVEL_ID)))
                .willThrow(new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"));

        mockMvc.perform(settingsRequest(NOVEL_ID).with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(NOVEL_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(NOVEL_NOT_FOUND.getDescription()))
                .andDo(document("novel-notification-novel-not-found",
                        resource(settingsError().build())));
    }

    private ResourceSnippetParametersBuilder settingsError() {
        return settings()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder settings() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION)
                .pathParameters(parameterWithName("novelId").description("조회할 작품 ID. 양수여야 한다."));
    }

    private List<FieldDescriptor> settingsResponseFields() {
        return List.of(
                fieldWithPath("isCompletionNotificationEnabled").type(BOOLEAN)
                        .description("완결 알림 등록 여부"),
                fieldWithPath("isHiatusReturnNotificationEnabled").type(BOOLEAN)
                        .description("휴재 복귀 알림 등록 여부"));
    }

    private MockHttpServletRequestBuilder settingsRequest(Long novelId) {
        return get("/novels/{novelId}/notification", novelId);
    }
}
