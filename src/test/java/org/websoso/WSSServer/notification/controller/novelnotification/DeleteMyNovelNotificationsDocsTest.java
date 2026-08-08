package org.websoso.WSSServer.notification.controller.novelnotification;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.exception.error.CustomAuthError.ACCESS_TOKEN_EXPIRED;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomAuthError.WRONG_TOKEN_TYPE;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOTIFICATION_TYPE_NOT_NULL;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_IDS_MAX_SIZE;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_IDS_NOT_EMPTY;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.NOVEL_ID_POSITIVE;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;
import static org.websoso.WSSServer.support.docs.RequestPreprocessors.withoutRequestBody;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.notification.application.NovelNotificationApplication;
import org.websoso.WSSServer.notification.controller.NovelNotificationController;
import org.websoso.WSSServer.notification.controller.request.NovelNotificationDeleteRequest;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code DELETE /users/me/notification/novels}의 실제 응답을 문서화하는 REST Docs 테스트.
 * 설정 화면에서 선택한 같은 유형의 작품 알림을 한 번에 해제하는 API다.
 *
 * <p>등록되어 있지 않은 작품 ID가 섞여 있어도 성공으로 응답하는 멱등 API이므로,
 * 클라이언트가 삭제 전에 등록 여부를 다시 확인할 필요가 없다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(NovelNotificationController.class)
class DeleteMyNovelNotificationsDocsTest {

    private static final Long USER_ID = 42L;
    private static final int MAX_NOVEL_IDS = 100;
    private static final NovelNotificationDeleteRequest DELETE_REQUEST =
            new NovelNotificationDeleteRequest(NovelNotificationType.COMPLETION, List.of(1L, 2L));
    private static final String TAG = "Novel Notification";
    private static final String SUMMARY = "작품 알림 구독 일괄 삭제";
    private static final Schema REQUEST_SCHEMA = Schema.schema("NovelNotificationDeleteRequest");
    private static final String MALFORMED_JSON_MESSAGE = "잘못된 JSON 형식입니다.";

    private static final String DESCRIPTION = String.join("\n",
            "설정 화면에서 선택한 같은 유형의 작품 알림을 한 번에 해제합니다.",
            "",
            "한 번에 100개까지 보낼 수 있고, 알림 유형이 다른 작품은 함께 삭제할 수 없습니다.",
            "등록되어 있지 않은 작품 ID가 섞여 있어도 성공하는 멱등 API입니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 204 No Content — 성공. (응답 본문이 없습니다.)",
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), NOTIFICATION_TYPE_NOT_NULL),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), NOVEL_IDS_NOT_EMPTY),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), NOVEL_IDS_MAX_SIZE),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), NOVEL_ID_POSITIVE),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), MALFORMED_JSON_MESSAGE)
                    + " (본문이 JSON으로 읽히지 않을 때 반환합니다.)",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
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

    @DisplayName("작품 알림 구독 일괄 삭제 성공(204, 본문 없음) 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsSuccess() throws Exception {
        mockMvc.perform(deleteRequest(DELETE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("my-novel-notifications-delete",
                        resource(deletion()
                                .requestSchema(REQUEST_SCHEMA)
                                .requestFields(deleteRequestFields())
                                .build())));
    }

    @DisplayName("알림 유형이 없는 요청의 400 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithoutNotificationType() throws Exception {
        mockMvc.perform(deleteRequest(new NovelNotificationDeleteRequest(null, List.of(1L)))
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NOTIFICATION_TYPE_NOT_NULL))
                .andDo(document("my-novel-notifications-delete-notification-type-null",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("삭제할 작품이 하나도 없는 요청의 400 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithEmptyNovelIds() throws Exception {
        mockMvc.perform(deleteRequest(
                        new NovelNotificationDeleteRequest(NovelNotificationType.COMPLETION, List.of()))
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NOVEL_IDS_NOT_EMPTY))
                .andDo(document("my-novel-notifications-delete-novel-ids-empty",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("삭제할 작품이 100개를 넘는 요청의 400 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithTooManyNovelIds() throws Exception {
        List<Long> tooManyNovelIds = LongStream.rangeClosed(1, MAX_NOVEL_IDS + 1).boxed().toList();

        mockMvc.perform(deleteRequest(
                        new NovelNotificationDeleteRequest(NovelNotificationType.COMPLETION, tooManyNovelIds))
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NOVEL_IDS_MAX_SIZE))
                .andDo(document("my-novel-notifications-delete-novel-ids-too-many",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("양수가 아닌 작품 ID가 섞인 요청의 400 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithNonPositiveNovelId() throws Exception {
        mockMvc.perform(deleteRequest(
                        new NovelNotificationDeleteRequest(NovelNotificationType.COMPLETION, List.of(-1L)))
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NOVEL_ID_POSITIVE))
                .andDo(document("my-novel-notifications-delete-novel-id-not-positive",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("본문 JSON 형식이 잘못된 요청의 400 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithMalformedJson() throws Exception {
        mockMvc.perform(delete("/users/me/notification/novels")
                        .contentType(APPLICATION_JSON)
                        .content("{\"notificationType\":")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(MALFORMED_JSON_MESSAGE))
                .andDo(document("my-novel-notifications-delete-malformed-json",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithExpiredAccessToken() throws Exception {
        mockMvc.perform(deleteRequest(DELETE_REQUEST).with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("my-novel-notifications-delete-access-token-expired",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithInvalidToken() throws Exception {
        mockMvc.perform(deleteRequest(DELETE_REQUEST))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("my-novel-notifications-delete-invalid-token",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithWrongTokenType() throws Exception {
        mockMvc.perform(deleteRequest(DELETE_REQUEST).with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("my-novel-notifications-delete-wrong-token-type",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentDeleteSubscriptionsWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(deleteRequest(DELETE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("my-novel-notifications-delete-user-not-found",
                        withoutRequestBody(),
                        resource(deletionError().build())));
    }

    private ResourceSnippetParametersBuilder deletionError() {
        return deletion()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder deletion() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION);
    }

    private List<FieldDescriptor> deleteRequestFields() {
        return List.of(
                fieldWithPath("notificationType").type(STRING)
                        .description("삭제할 알림 유형. COMPLETION 또는 HIATUS_RETURN."),
                fieldWithPath("novelIds").type(ARRAY)
                        .description("삭제할 작품 ID 목록. 1개 이상 100개 이하이며 각 값은 양수여야 한다."));
    }

    private MockHttpServletRequestBuilder deleteRequest(NovelNotificationDeleteRequest request)
            throws Exception {
        return delete("/users/me/notification/novels")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
    }
}
