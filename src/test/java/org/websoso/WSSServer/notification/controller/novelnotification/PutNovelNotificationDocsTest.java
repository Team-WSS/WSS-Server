package org.websoso.WSSServer.notification.controller.novelnotification;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.COMPLETION_NOTIFICATION_NOT_NULL;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.HIATUS_RETURN_NOTIFICATION_NOT_NULL;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_ID_POSITIVE;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.tamperedAccessToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;
import static org.websoso.WSSServer.support.docs.RequestPreprocessors.withoutRequestBody;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_FIELD;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_PARAMETER;
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
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.notification.application.NovelNotificationApplication;
import org.websoso.WSSServer.notification.controller.NovelNotificationController;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationUpdateRequest;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code PUT /novels/{novelId}/notification}의 실제 응답을 문서화하는 REST Docs 테스트.
 * 두 알림 상태를 요청한 값과 동일하게 맞추는 멱등 API이므로, 같은 요청을 반복해도 결과가 같다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(NovelNotificationController.class)
class PutNovelNotificationDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long NOVEL_ID = 1L;
    private static final Long INVALID_NOVEL_ID = 0L;
    private static final NovelNotificationUpdateRequest UPDATE_REQUEST =
            new NovelNotificationUpdateRequest(true, false);
    private static final String TAG = "Novel Notification";
    private static final String SUMMARY = "작품 알림 설정 변경";
    private static final Schema REQUEST_SCHEMA = Schema.schema("NovelNotificationUpdateRequest");

    private static final String DESCRIPTION = String.join("\n",
            "작품 상세에서 완결·휴재 복귀 알림을 요청한 상태로 맞춥니다.",
            "",
            "두 값을 모두 보내야 하며, 각각 true면 등록하고 false면 해제합니다.",
            "이미 같은 상태여도 성공하는 멱등 API이므로, 토글 결과를 그대로 보내면 됩니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 204 No Content — 성공. (응답 본문이 없습니다.)",
            errorLine(INVALID_REQUEST_PARAMETER, NOVEL_ID_POSITIVE),
            errorLine(INVALID_REQUEST_FIELD, COMPLETION_NOTIFICATION_NOT_NULL),
            errorLine(INVALID_REQUEST_FIELD, HIATUS_RETURN_NOTIFICATION_NOT_NULL),
            errorLine(MALFORMED_REQUEST_BODY) + " (본문이 JSON으로 읽히지 않을 때 반환합니다.)",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            errorLine(NOVEL_NOT_FOUND),
            "",
            "400과 401은 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private NovelNotificationApplication novelNotificationApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("작품 알림 설정 변경 성공(204, 본문 없음) 응답을 문서화한다")
    @Test
    void documentUpdateSettingsSuccess() throws Exception {
        mockMvc.perform(updateRequest(NOVEL_ID, UPDATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("novel-notification-update",
                        resource(update()
                                .requestSchema(REQUEST_SCHEMA)
                                .requestFields(updateRequestFields())
                                .build())));
    }

    @DisplayName("양수가 아닌 작품 ID 요청의 400 COMMON-003 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithNonPositiveNovelId() throws Exception {
        mockMvc.perform(updateRequest(INVALID_NOVEL_ID, UPDATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_PARAMETER.getCode()))
                .andExpect(jsonPath("$.message").value(NOVEL_ID_POSITIVE))
                .andDo(document("novel-notification-update-novel-id-not-positive",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    @DisplayName("완결 알림 값이 없는 요청의 400 COMMON-001 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithoutCompletionValue() throws Exception {
        mockMvc.perform(updateRequest(NOVEL_ID, new NovelNotificationUpdateRequest(null, false))
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_FIELD.getCode()))
                .andExpect(jsonPath("$.message").value(COMPLETION_NOTIFICATION_NOT_NULL))
                .andDo(document("novel-notification-update-completion-null",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    @DisplayName("휴재 복귀 알림 값이 없는 요청의 400 COMMON-001 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithoutHiatusReturnValue() throws Exception {
        mockMvc.perform(updateRequest(NOVEL_ID, new NovelNotificationUpdateRequest(true, null))
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_FIELD.getCode()))
                .andExpect(jsonPath("$.message").value(HIATUS_RETURN_NOTIFICATION_NOT_NULL))
                .andDo(document("novel-notification-update-hiatus-return-null",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    @DisplayName("본문 JSON 형식이 잘못된 요청의 400 COMMON-002 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithMalformedJson() throws Exception {
        mockMvc.perform(put("/novels/{novelId}/notification", NOVEL_ID)
                        .contentType(APPLICATION_JSON)
                        .content("{\"isCompletionNotificationEnabled\":")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(MALFORMED_REQUEST_BODY.getCode()))
                .andExpect(jsonPath("$.message").value(MALFORMED_REQUEST_BODY.getDescription()))
                .andDo(document("novel-notification-update-malformed-json",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithExpiredAccessToken() throws Exception {
        mockMvc.perform(updateRequest(NOVEL_ID, UPDATE_REQUEST).with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("novel-notification-update-access-token-expired",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    /**
     * 헤더를 아예 빼지 않고 변조된 토큰을 보낸다.
     * 이 경로·메서드의 문서 중 Bearer 헤더가 하나도 없는 요청이 섞이면 생성기가 operation의
     * security requirement를 만들지 않아 Swagger UI에 Authorize 자물쇠가 붙지 않았다.
     * AUTH-001은 헤더 누락과 형식 오류를 함께 뜻하므로 변조 토큰으로도 같은 응답을 재현한다.
     */
    @DisplayName("변조된 Access Token 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithInvalidToken() throws Exception {
        mockMvc.perform(updateRequest(NOVEL_ID, UPDATE_REQUEST).with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("novel-notification-update-invalid-token",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithWrongTokenType() throws Exception {
        mockMvc.perform(updateRequest(NOVEL_ID, UPDATE_REQUEST).with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("novel-notification-update-wrong-token-type",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(updateRequest(NOVEL_ID, UPDATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("novel-notification-update-user-not-found",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    @DisplayName("존재하지 않는 작품 요청의 404 NOVEL-001 응답을 문서화한다")
    @Test
    void documentUpdateSettingsWithUnknownNovel() throws Exception {
        willThrow(new CustomNovelException(NOVEL_NOT_FOUND, "novel with the given id is not found"))
                .given(novelNotificationApplication).updateSettings(any(User.class), eq(NOVEL_ID), eq(UPDATE_REQUEST));

        mockMvc.perform(updateRequest(NOVEL_ID, UPDATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(NOVEL_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(NOVEL_NOT_FOUND.getDescription()))
                .andDo(document("novel-notification-update-novel-not-found",
                        withoutRequestBody(),
                        resource(updateError().build())));
    }

    private ResourceSnippetParametersBuilder updateError() {
        return update()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder update() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION)
                .pathParameters(parameterWithName("novelId").description("설정을 변경할 작품 ID. 양수여야 한다."));
    }

    private List<FieldDescriptor> updateRequestFields() {
        return List.of(
                fieldWithPath("isCompletionNotificationEnabled").type(BOOLEAN)
                        .description("완결 알림을 등록할지 여부. true면 등록하고 false면 해제한다."),
                fieldWithPath("isHiatusReturnNotificationEnabled").type(BOOLEAN)
                        .description("휴재 복귀 알림을 등록할지 여부. true면 등록하고 false면 해제한다."));
    }

    private MockHttpServletRequestBuilder updateRequest(Long novelId, NovelNotificationUpdateRequest request)
            throws Exception {
        return put("/novels/{novelId}/notification", novelId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
    }
}
