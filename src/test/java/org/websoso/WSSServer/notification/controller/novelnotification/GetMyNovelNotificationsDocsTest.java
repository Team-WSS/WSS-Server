package org.websoso.WSSServer.notification.controller.novelnotification;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.key;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.exception.error.CustomAuthError.ACCESS_TOKEN_EXPIRED;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomAuthError.WRONG_TOKEN_TYPE;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.LAST_SUBSCRIPTION_ID_POSITIVE_OR_ZERO;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.SIZE_MAX;
import static org.websoso.WSSServer.notification.controller.NovelNotificationValidationMessage.SIZE_MIN;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import java.util.Arrays;
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
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.notification.application.NovelNotificationApplication;
import org.websoso.WSSServer.notification.controller.NovelNotificationController;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationPageResponse;
import org.websoso.WSSServer.notification.controller.response.NovelNotificationPageResponse.NovelNotificationItem;
import org.websoso.WSSServer.notification.domain.NovelNotificationType;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code GET /users/me/notification/novels}의 실제 응답을 문서화하는 REST Docs 테스트.
 * 설정 화면에서 알림 유형별로 등록한 작품을 커서 방식으로 조회하는 API다.
 *
 * <p>필수 파라미터 누락과 Enum 변환 실패도 이 API의 계약이므로 함께 문서화한다.
 * 두 경우 모두 처리되지 않으면 401 AUTH-001로 둔갑해 원인을 알 수 없다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(NovelNotificationController.class)
class GetMyNovelNotificationsDocsTest {

    private static final Long USER_ID = 42L;
    private static final String TAG = "Novel Notification";
    private static final String SUMMARY = "작품 알림 구독 목록 조회";
    private static final Schema RESPONSE_SCHEMA = Schema.schema("NovelNotificationPageResponse");
    private static final List<String> NOTIFICATION_TYPE_VALUES =
            Arrays.stream(NovelNotificationType.values()).map(Enum::name).toList();
    private static final String NOTIFICATION_TYPE_REQUIRED_MESSAGE = "필수 요청 파라미터가 없습니다: notificationType";
    private static final String NOTIFICATION_TYPE_MISMATCH_MESSAGE = "요청 값의 형식이 올바르지 않습니다: notificationType";

    private static final String DESCRIPTION = String.join("\n",
            "설정 화면에서 사용자가 등록한 작품 알림을 유형별로 조회합니다.",
            "",
            "알림이 발송된 작품은 목록에서 제외됩니다. 아직 발송되지 않은 등록만 내려갑니다.",
            "",
            "최신 등록순으로 내려가며 커서 방식으로 페이지를 이어 붙입니다.",
            "첫 페이지는 lastSubscriptionId를 생략하거나 0으로 보내고, 다음 페이지는 응답의 nextSubscriptionId를 그대로 넣습니다.",
            "isLoadable이 false면 더 조회할 항목이 없고, 이때 nextSubscriptionId는 null입니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), NOTIFICATION_TYPE_REQUIRED_MESSAGE),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), NOTIFICATION_TYPE_MISMATCH_MESSAGE)
                    + " (COMPLETION, HIATUS_RETURN 외의 값을 보냈을 때 반환합니다.)",
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), LAST_SUBSCRIPTION_ID_POSITIVE_OR_ZERO),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), SIZE_MIN),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), SIZE_MAX),
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            "",
            "400과 401은 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 본문 확인이 필요합니다.");

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

    @DisplayName("작품 알림 구독 목록을 조회하는 200 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsSuccess() throws Exception {
        given(novelNotificationApplication.getSubscriptions(
                any(User.class), any(NovelNotificationType.class), anyLong(), anyInt()))
                .willReturn(new NovelNotificationPageResponse(true, 11L, List.of(
                        new NovelNotificationItem(
                                12L, 1L, "https://image.websoso.kr/novel/1.jpg", "재혼 황후", "알파타르트", "2026.08.06"),
                        new NovelNotificationItem(
                                11L, 2L, "https://image.websoso.kr/novel/2.jpg", "전지적 독자 시점", "싱숑",
                                "2026.08.05"))));

        mockMvc.perform(subscriptionsRequest("COMPLETION").with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.isLoadable").value(true))
                .andExpect(jsonPath("$.nextSubscriptionId").value(11L))
                .andExpect(jsonPath("$.subscriptions[0].subscriptionId").value(12L))
                .andExpect(jsonPath("$.subscriptions[1].subscriptionId").value(11L))
                .andDo(document("my-novel-notifications",
                        resource(subscriptions()
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseFields(subscriptionsResponseFields())
                                .build())));
    }

    @DisplayName("알림 유형을 보내지 않은 요청의 400 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithoutNotificationType() throws Exception {
        mockMvc.perform(get("/users/me/notification/novels").with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NOTIFICATION_TYPE_REQUIRED_MESSAGE))
                .andDo(document("my-novel-notifications-notification-type-required",
                        resource(withoutQueryParameters()
                                .responseSchema(ERROR_RESULT_SCHEMA)
                                .responseFields(errorResultFields())
                                .build())));
    }

    @DisplayName("정의되지 않은 알림 유형 요청의 400 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithUnknownNotificationType() throws Exception {
        mockMvc.perform(subscriptionsRequest("UNKNOWN").with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NOTIFICATION_TYPE_MISMATCH_MESSAGE))
                .andDo(document("my-novel-notifications-notification-type-mismatch",
                        resource(subscriptionsError().build())));
    }

    @DisplayName("음수 커서 요청의 400 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithNegativeCursor() throws Exception {
        mockMvc.perform(get("/users/me/notification/novels")
                        .queryParam("notificationType", "COMPLETION")
                        .queryParam("lastSubscriptionId", "-1")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(LAST_SUBSCRIPTION_ID_POSITIVE_OR_ZERO))
                .andDo(document("my-novel-notifications-cursor-negative",
                        resource(subscriptionsError().build())));
    }

    @DisplayName("조회 개수가 1 미만인 요청의 400 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithTooSmallSize() throws Exception {
        mockMvc.perform(get("/users/me/notification/novels")
                        .queryParam("notificationType", "COMPLETION")
                        .queryParam("size", "0")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(SIZE_MIN))
                .andDo(document("my-novel-notifications-size-too-small",
                        resource(subscriptionsError().build())));
    }

    @DisplayName("조회 개수가 50을 넘는 요청의 400 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithTooLargeSize() throws Exception {
        mockMvc.perform(get("/users/me/notification/novels")
                        .queryParam("notificationType", "COMPLETION")
                        .queryParam("size", "51")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(SIZE_MAX))
                .andDo(document("my-novel-notifications-size-too-large",
                        resource(subscriptionsError().build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithExpiredAccessToken() throws Exception {
        mockMvc.perform(subscriptionsRequest("COMPLETION").with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("my-novel-notifications-access-token-expired",
                        resource(subscriptionsError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithInvalidToken() throws Exception {
        mockMvc.perform(subscriptionsRequest("COMPLETION"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("my-novel-notifications-invalid-token",
                        resource(subscriptionsError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithWrongTokenType() throws Exception {
        mockMvc.perform(subscriptionsRequest("COMPLETION").with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("my-novel-notifications-wrong-token-type",
                        resource(subscriptionsError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentGetSubscriptionsWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(subscriptionsRequest("COMPLETION").with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("my-novel-notifications-user-not-found",
                        resource(subscriptionsError().build())));
    }

    private ResourceSnippetParametersBuilder subscriptionsError() {
        return subscriptions()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    /**
     * 필수 파라미터 누락을 문서화할 때 쓴다. REST Docs는 선언한 query parameter가 요청에 없으면
     * snippet 생성을 실패시키므로, 이 경우에만 파라미터 서술을 붙이지 않는다.
     */
    private ResourceSnippetParametersBuilder withoutQueryParameters() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION);
    }

    /**
     * enumValues 속성을 주면 생성 명세의 파라미터 스키마에 enum이 붙어
     * Swagger UI가 자유 입력 대신 선택 상자를 띄운다.
     */
    private ResourceSnippetParametersBuilder subscriptions() {
        return withoutQueryParameters()
                .queryParameters(
                        parameterWithName("notificationType")
                                .type(SimpleType.STRING)
                                .attributes(key("enumValues").value(NOTIFICATION_TYPE_VALUES))
                                .description("조회할 알림 유형"),
                        parameterWithName("lastSubscriptionId").optional()
                                .type(SimpleType.INTEGER)
                                .defaultValue(0)
                                .description("이전 페이지 마지막 구독 ID. 첫 페이지는 생략하거나 0을 보낸다."),
                        parameterWithName("size").optional()
                                .type(SimpleType.INTEGER)
                                .defaultValue(10)
                                .description("조회 개수. 1 이상 50 이하."));
    }

    private List<FieldDescriptor> subscriptionsResponseFields() {
        return List.of(
                fieldWithPath("isLoadable").type(BOOLEAN).description("다음 페이지가 있는지 여부"),
                fieldWithPath("nextSubscriptionId").type(NUMBER).optional()
                        .description("다음 페이지 요청에 넣을 커서. 다음 페이지가 없으면 null이다."),
                fieldWithPath("subscriptions[]").type(org.springframework.restdocs.payload.JsonFieldType.ARRAY)
                        .description("등록한 작품 알림 목록"),
                fieldWithPath("subscriptions[].subscriptionId").type(NUMBER).description("구독 ID"),
                fieldWithPath("subscriptions[].novelId").type(NUMBER).description("작품 ID"),
                fieldWithPath("subscriptions[].novelImage").type(STRING).description("작품 이미지 URL"),
                fieldWithPath("subscriptions[].novelTitle").type(STRING).description("작품 제목"),
                fieldWithPath("subscriptions[].novelAuthor").type(STRING).description("작가명"),
                fieldWithPath("subscriptions[].registeredDate").type(STRING)
                        .description("알림을 등록한 날짜. yyyy.MM.dd 형식."));
    }

    private MockHttpServletRequestBuilder subscriptionsRequest(String notificationType) {
        return get("/users/me/notification/novels")
                .queryParam("notificationType", notificationType)
                .queryParam("lastSubscriptionId", "0")
                .queryParam("size", "10");
    }
}
