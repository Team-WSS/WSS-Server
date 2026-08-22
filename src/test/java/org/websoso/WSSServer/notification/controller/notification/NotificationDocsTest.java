package org.websoso.WSSServer.notification.controller.notification;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
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
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_READ_FORBIDDEN;
import static org.websoso.WSSServer.exception.error.CustomNotificationError.NOTIFICATION_TYPE_INVALID;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.tamperedAccessToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;

import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
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
import org.websoso.WSSServer.application.NotificationApplication;
import org.websoso.WSSServer.exception.exception.CustomNotificationException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.notification.controller.NotificationController;
import org.websoso.WSSServer.notification.controller.response.NotificationDetailResponse;
import org.websoso.WSSServer.notification.controller.response.NotificationPageResponse;
import org.websoso.WSSServer.notification.controller.response.NotificationPageResponse.NotificationItem;
import org.websoso.WSSServer.notification.controller.response.NotificationReadStatusResponse;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/** 노티피케이션 Controller의 운영 API 4개를 문서화하는 REST Docs 테스트. */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(NotificationController.class)
class NotificationDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long NOTIFICATION_ID = 10L;
    private static final String TAG = "Notification";
    private static final Schema PAGE_RESPONSE_SCHEMA = Schema.schema("NotificationPageResponse");
    private static final Schema DETAIL_RESPONSE_SCHEMA = Schema.schema("NotificationDetailResponse");
    private static final Schema STATUS_RESPONSE_SCHEMA = Schema.schema("NotificationReadStatusResponse");

    private static final String LIST_DESCRIPTION = String.join("\n",
            "사용자에게 발송된 알림을 최신순으로 조회합니다.",
            "첫 페이지는 lastNotificationId를 생략하거나 0으로 보내고, 다음 페이지는 마지막 notificationId를 커서로 보냅니다.",
            "피드 알림은 feedId, 작품 완결·휴재 복귀 알림은 novelId가 이동 대상이며 나머지 이동 ID는 null입니다.",
            "isLoadable이 false면 다음 페이지가 없습니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            "- 400 BAD_REQUEST — 커서가 음수이거나 size가 1 미만 또는 50 초과인 요청.",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN),
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND));

    private static final String DETAIL_DESCRIPTION = String.join("\n",
            "공지·이벤트 알림의 상세 내용을 조회하고 해당 알림을 읽음 처리합니다.",
            "피드 또는 작품 이동 알림은 상세 조회 대상이 아니며 NOTIFICATION-003을 반환합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            "- 400 BAD_REQUEST — notificationId가 양수가 아닌 요청.",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN),
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(NOTIFICATION_TYPE_INVALID),
            errorLine(NOTIFICATION_NOT_FOUND),
            errorLine(USER_NOT_FOUND));

    private static final String STATUS_DESCRIPTION = String.join("\n",
            "사용자에게 읽지 않은 알림이 하나라도 있는지 조회합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN),
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND));

    private static final String READ_DESCRIPTION = String.join("\n",
            "사용자에게 발송된 알림을 읽음 상태로 변경합니다.",
            "이미 읽은 알림을 다시 요청해도 성공하는 멱등 API입니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 204 No Content — 성공. (응답 본문이 없습니다.)",
            "- 400 BAD_REQUEST — notificationId가 양수가 아닌 요청.",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN),
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(NOTIFICATION_READ_FORBIDDEN),
            errorLine(NOTIFICATION_NOT_FOUND),
            errorLine(USER_NOT_FOUND));

    private static final String LEGACY_STATUS_DESCRIPTION = String.join("\n",
            "Deprecated API입니다. 신규 구현에서는 GET /notifications/status를 사용합니다.",
            "사용자에게 읽지 않은 알림이 하나라도 있는지 조회합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            errorLine(INVALID_TOKEN));

    private static final String LEGACY_READ_DESCRIPTION = String.join("\n",
            "Deprecated API입니다. 신규 구현에서는 PATCH /notifications/{notificationId}/read-status를 사용합니다.",
            "사용자에게 발송된 알림을 읽음 상태로 변경합니다.",
            "신규 API와 달리 성공 시 201 Created를 반환합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 201 Created — 성공. (응답 본문이 없습니다.)",
            errorLine(INVALID_TOKEN));

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private NotificationApplication notificationApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("피드와 작품 알림을 포함한 알림 목록 200 응답을 문서화한다")
    @Test
    void documentGetNotificationsSuccess() throws Exception {
        given(notificationApplication.getNotifications(any(User.class), eq(0L), eq(10)))
                .willReturn(notificationPageResponse());

        mockMvc.perform(notificationsRequest().with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.isLoadable").value(true))
                .andExpect(jsonPath("$.notifications[0].feedId").value(31L))
                .andExpect(jsonPath("$.notifications[0].novelId").isEmpty())
                .andExpect(jsonPath("$.notifications[1].feedId").isEmpty())
                .andExpect(jsonPath("$.notifications[1].novelId").value(7L))
                .andDo(document("notifications-get",
                        resource(notificationList()
                                .responseSchema(PAGE_RESPONSE_SCHEMA)
                                .responseFields(notificationPageFields())
                                .build())));
    }

    @DisplayName("목록 파라미터를 생략하면 첫 커서와 기본 조회 개수를 사용한다")
    @Test
    void getNotificationsWithDefaultParameters() throws Exception {
        given(notificationApplication.getNotifications(any(User.class), isNull(), eq(10)))
                .willReturn(new NotificationPageResponse(false, List.of()));

        mockMvc.perform(get("/notifications").with(accessToken(USER_ID)))
                .andExpect(status().isOk());

        then(notificationApplication).should().getNotifications(any(User.class), isNull(), eq(10));
    }

    @DisplayName("음수 커서의 400 응답을 문서화한다")
    @Test
    void documentGetNotificationsWithNegativeCursor() throws Exception {
        mockMvc.perform(get("/notifications")
                        .queryParam("lastNotificationId", "-1")
                        .queryParam("size", "10")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andDo(document("notifications-get-cursor-negative",
                        resource(notificationListError().build())));
    }

    @DisplayName("1 미만 조회 개수의 400 응답을 문서화한다")
    @Test
    void documentGetNotificationsWithTooSmallSize() throws Exception {
        documentListValidationError("0", "notifications-get-size-too-small");
    }

    @DisplayName("50 초과 조회 개수의 400 응답을 문서화한다")
    @Test
    void documentGetNotificationsWithTooLargeSize() throws Exception {
        documentListValidationError("51", "notifications-get-size-too-large");
    }

    @DisplayName("알림 목록 요청의 만료 토큰 401 응답을 문서화한다")
    @Test
    void documentGetNotificationsWithExpiredToken() throws Exception {
        mockMvc.perform(notificationsRequest().with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andDo(document("notifications-get-access-token-expired",
                        resource(notificationListError().build())));
    }

    @DisplayName("알림 목록 요청의 변조 토큰 401 응답을 문서화한다")
    @Test
    void documentGetNotificationsWithInvalidToken() throws Exception {
        mockMvc.perform(notificationsRequest().with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andDo(document("notifications-get-invalid-token",
                        resource(notificationListError().build())));
    }

    @DisplayName("알림 목록 요청의 Refresh Token 401 응답을 문서화한다")
    @Test
    void documentGetNotificationsWithWrongTokenType() throws Exception {
        mockMvc.perform(notificationsRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andDo(document("notifications-get-wrong-token-type",
                        resource(notificationListError().build())));
    }

    @DisplayName("공지 알림 상세 조회 200 응답을 문서화한다")
    @Test
    void documentGetNotificationDetailSuccess() throws Exception {
        given(notificationApplication.getNotificationDetail(any(User.class), eq(NOTIFICATION_ID)))
                .willReturn(new NotificationDetailResponse("서비스 점검 안내", "2026.08.22", "점검 상세 내용입니다."));

        mockMvc.perform(notificationDetailRequest().with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.notificationTitle").value("서비스 점검 안내"))
                .andExpect(jsonPath("$.notificationCreatedDate").value("2026.08.22"))
                .andExpect(jsonPath("$.notificationDetail").value("점검 상세 내용입니다."))
                .andDo(document("notification-detail-get",
                        resource(notificationDetail()
                                .responseSchema(DETAIL_RESPONSE_SCHEMA)
                                .responseFields(notificationDetailFields())
                                .build())));
    }

    @DisplayName("양수가 아닌 상세 알림 ID의 400 응답을 문서화한다")
    @Test
    void documentGetNotificationDetailWithNonPositiveId() throws Exception {
        mockMvc.perform(get("/notifications/{notificationId}", 0L).with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andDo(document("notification-detail-get-id-not-positive",
                        resource(notificationDetailError().build())));
    }

    @DisplayName("존재하지 않는 공지 알림의 404 응답을 문서화한다")
    @Test
    void documentGetNotificationDetailNotFound() throws Exception {
        given(notificationApplication.getNotificationDetail(any(User.class), eq(NOTIFICATION_ID)))
                .willThrow(new CustomNotificationException(NOTIFICATION_NOT_FOUND, "notification not found"));

        documentDetailError(NOTIFICATION_NOT_FOUND, "notification-detail-get-not-found");
    }

    @DisplayName("공지 유형이 아닌 알림 상세 요청의 400 응답을 문서화한다")
    @Test
    void documentGetNotificationDetailWithInvalidType() throws Exception {
        given(notificationApplication.getNotificationDetail(any(User.class), eq(NOTIFICATION_ID)))
                .willThrow(new CustomNotificationException(NOTIFICATION_TYPE_INVALID, "invalid notification type"));

        documentDetailError(NOTIFICATION_TYPE_INVALID, "notification-detail-get-type-invalid");
    }

    @DisplayName("공지 알림 상세 요청의 변조 토큰 401 응답을 문서화한다")
    @Test
    void documentGetNotificationDetailWithInvalidToken() throws Exception {
        mockMvc.perform(notificationDetailRequest().with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andDo(document("notification-detail-get-invalid-token",
                        resource(notificationDetailError().build())));
    }

    @DisplayName("읽지 않은 알림 상태 조회 200 응답을 문서화한다")
    @Test
    void documentGetNotificationStatusSuccess() throws Exception {
        given(notificationApplication.getReadStatus(any(User.class)))
                .willReturn(new NotificationReadStatusResponse(true));

        mockMvc.perform(get("/notifications/status").with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.hasUnreadNotifications").value(true))
                .andDo(document("notification-status-get",
                        resource(notificationStatus()
                                .responseSchema(STATUS_RESPONSE_SCHEMA)
                                .responseFields(notificationStatusFields())
                                .build())));
    }

    @DisplayName("알림 상태 요청의 변조 토큰 401 응답을 문서화한다")
    @Test
    void documentGetNotificationStatusWithInvalidToken() throws Exception {
        mockMvc.perform(get("/notifications/status").with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andDo(document("notification-status-get-invalid-token",
                        resource(notificationStatusError().build())));
    }

    @DisplayName("알림 상태 요청의 사용자가 없으면 404 응답을 문서화한다")
    @Test
    void documentGetNotificationStatusWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user not found"));

        mockMvc.perform(get("/notifications/status").with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andDo(document("notification-status-get-user-not-found",
                        resource(notificationStatusError().build())));
    }

    @DisplayName("알림 읽음 처리 204 응답을 문서화한다")
    @Test
    void documentUpdateNotificationReadStatusSuccess() throws Exception {
        mockMvc.perform(notificationReadRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("notification-read-status-patch",
                        resource(notificationRead().build())));
    }

    @DisplayName("양수가 아닌 읽음 처리 알림 ID의 400 응답을 문서화한다")
    @Test
    void documentUpdateNotificationReadStatusWithNonPositiveId() throws Exception {
        mockMvc.perform(patch("/notifications/{notificationId}/read-status", 0L)
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andDo(document("notification-read-status-patch-id-not-positive",
                        resource(notificationReadError().build())));
    }

    @DisplayName("존재하지 않는 알림 읽음 요청의 404 응답을 문서화한다")
    @Test
    void documentUpdateNotificationReadStatusNotFound() throws Exception {
        willThrow(new CustomNotificationException(NOTIFICATION_NOT_FOUND, "notification not found"))
                .given(notificationApplication)
                .updateNotificationReadStatus(any(User.class), eq(NOTIFICATION_ID));

        documentReadError(NOTIFICATION_NOT_FOUND, "notification-read-status-patch-not-found");
    }

    @DisplayName("다른 사용자의 알림 읽음 요청의 403 응답을 문서화한다")
    @Test
    void documentUpdateNotificationReadStatusForbidden() throws Exception {
        willThrow(new CustomNotificationException(NOTIFICATION_READ_FORBIDDEN, "notification read forbidden"))
                .given(notificationApplication)
                .updateNotificationReadStatus(any(User.class), eq(NOTIFICATION_ID));

        documentReadError(NOTIFICATION_READ_FORBIDDEN, "notification-read-status-patch-forbidden");
    }

    @DisplayName("알림 읽음 요청의 변조 토큰 401 응답을 문서화한다")
    @Test
    void documentUpdateNotificationReadStatusWithInvalidToken() throws Exception {
        mockMvc.perform(notificationReadRequest().with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andDo(document("notification-read-status-patch-invalid-token",
                        resource(notificationReadError().build())));
    }

    @DisplayName("기존 읽지 않은 알림 상태 조회 200 응답을 Deprecated API로 문서화한다")
    @Test
    void documentGetNotificationStatusDeprecatedSuccess() throws Exception {
        given(notificationApplication.getReadStatus(any(User.class)))
                .willReturn(new NotificationReadStatusResponse(true));

        mockMvc.perform(get("/notifications/unread").with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.hasUnreadNotifications").value(true))
                .andDo(document("notification-unread-get-deprecated",
                        resource(notificationLegacyStatus()
                                .responseSchema(STATUS_RESPONSE_SCHEMA)
                                .responseFields(notificationStatusFields())
                                .build())));
    }

    @DisplayName("기존 알림 상태 조회의 변조 토큰 401 응답을 문서화한다")
    @Test
    void documentGetNotificationStatusDeprecatedWithInvalidToken() throws Exception {
        mockMvc.perform(get("/notifications/unread").with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andDo(document("notification-unread-get-deprecated-invalid-token",
                        resource(notificationLegacyStatus()
                                .responseSchema(ERROR_RESULT_SCHEMA)
                                .responseFields(errorResultFields())
                                .build())));
    }

    @DisplayName("기존 알림 읽음 처리 201 응답을 Deprecated API로 문서화한다")
    @Test
    void documentCreateNotificationAsReadDeprecatedSuccess() throws Exception {
        mockMvc.perform(notificationLegacyReadRequest().with(accessToken(USER_ID)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""))
                .andDo(document("notification-read-post-deprecated",
                        resource(notificationLegacyRead().build())));
    }

    @DisplayName("기존 알림 읽음 처리의 변조 토큰 401 응답을 문서화한다")
    @Test
    void documentCreateNotificationAsReadDeprecatedWithInvalidToken() throws Exception {
        mockMvc.perform(notificationLegacyReadRequest().with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andDo(document("notification-read-post-deprecated-invalid-token",
                        resource(notificationLegacyRead()
                                .responseSchema(ERROR_RESULT_SCHEMA)
                                .responseFields(errorResultFields())
                                .build())));
    }

    @DisplayName("유효한 토큰의 사용자가 없으면 404 응답을 문서화한다")
    @Test
    void documentUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user not found"));

        mockMvc.perform(notificationsRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andDo(document("notifications-get-user-not-found",
                        resource(notificationListError().build())));
    }

    private void documentListValidationError(String size, String identifier) throws Exception {
        mockMvc.perform(get("/notifications")
                        .queryParam("lastNotificationId", "0")
                        .queryParam("size", size)
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andDo(document(identifier, resource(notificationListError().build())));
    }

    private void documentDetailError(ICustomError error, String identifier) throws Exception {
        mockMvc.perform(notificationDetailRequest().with(accessToken(USER_ID)))
                .andExpect(status().is(error.getStatusCode().value()))
                .andExpect(jsonPath("$.code").value(error.getCode()))
                .andExpect(jsonPath("$.message").value(error.getDescription()))
                .andDo(document(identifier, resource(notificationDetailError().build())));
    }

    private void documentReadError(ICustomError error, String identifier) throws Exception {
        mockMvc.perform(notificationReadRequest().with(accessToken(USER_ID)))
                .andExpect(status().is(error.getStatusCode().value()))
                .andExpect(jsonPath("$.code").value(error.getCode()))
                .andExpect(jsonPath("$.message").value(error.getDescription()))
                .andDo(document(identifier, resource(notificationReadError().build())));
    }

    private ResourceSnippetParametersBuilder notificationListError() {
        return notificationList()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder notificationList() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary("알림 목록 조회")
                .description(LIST_DESCRIPTION)
                .queryParameters(notificationQueryParameters());
    }

    private ResourceSnippetParametersBuilder notificationDetailError() {
        return notificationDetail()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder notificationDetail() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary("공지 알림 상세 조회")
                .description(DETAIL_DESCRIPTION)
                .pathParameters(notificationIdParameter("조회할 공지 알림 ID"));
    }

    private ResourceSnippetParametersBuilder notificationStatusError() {
        return notificationStatus()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder notificationStatus() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary("읽지 않은 알림 상태 조회")
                .description(STATUS_DESCRIPTION);
    }

    private ResourceSnippetParametersBuilder notificationReadError() {
        return notificationRead()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder notificationRead() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary("알림 읽음 처리")
                .description(READ_DESCRIPTION)
                .pathParameters(notificationIdParameter("읽음 처리할 알림 ID"));
    }

    private ResourceSnippetParametersBuilder notificationLegacyStatus() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary("[Deprecated] 읽지 않은 알림 상태 조회")
                .description(LEGACY_STATUS_DESCRIPTION)
                .deprecated(true);
    }

    private ResourceSnippetParametersBuilder notificationLegacyRead() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary("[Deprecated] 알림 읽음 처리")
                .description(LEGACY_READ_DESCRIPTION)
                .pathParameters(notificationIdParameter("읽음 처리할 알림 ID"))
                .deprecated(true);
    }

    private List<ParameterDescriptorWithType> notificationQueryParameters() {
        return List.of(
                parameterWithName("lastNotificationId").optional()
                        .type(SimpleType.INTEGER)
                        .defaultValue(0)
                        .description("이전 페이지의 마지막 알림 ID. 첫 페이지는 생략하거나 0을 보낸다."),
                parameterWithName("size").optional()
                        .type(SimpleType.INTEGER)
                        .defaultValue(10)
                        .description("조회 개수. 1 이상 50 이하."));
    }

    private ParameterDescriptorWithType notificationIdParameter(String description) {
        return parameterWithName("notificationId")
                .type(SimpleType.INTEGER)
                .description(description + ". 양수여야 한다.");
    }

    private List<FieldDescriptor> notificationPageFields() {
        return List.of(
                fieldWithPath("isLoadable").type(BOOLEAN).description("다음 페이지가 있는지 여부"),
                fieldWithPath("notifications").type(ARRAY).description("최신순 알림 목록"),
                fieldWithPath("notifications[].notificationId").type(NUMBER).description("알림 ID"),
                fieldWithPath("notifications[].notificationImage").type(STRING).description("알림 유형 이미지 URL"),
                fieldWithPath("notifications[].notificationTitle").type(STRING).description("알림 제목"),
                fieldWithPath("notifications[].notificationBody").type(STRING).description("알림 본문"),
                fieldWithPath("notifications[].createdDate").type(STRING)
                        .description("알림 생성 시각. 24시간 이내는 상대 시간, 이후는 yyyy.MM.dd 형식."),
                fieldWithPath("notifications[].isRead").type(BOOLEAN).description("읽음 여부"),
                fieldWithPath("notifications[].isNotice").type(BOOLEAN).description("공지·이벤트 알림 여부"),
                fieldWithPath("notifications[].feedId").type(NUMBER).optional()
                        .description("피드 이동 대상 ID. 피드 알림이 아니면 null이다."),
                fieldWithPath("notifications[].novelId").type(NUMBER).optional()
                        .description("작품 이동 대상 ID. 완결·휴재 복귀 알림이 아니면 null이다."));
    }

    private List<FieldDescriptor> notificationDetailFields() {
        return List.of(
                fieldWithPath("notificationTitle").type(STRING).description("공지 알림 제목"),
                fieldWithPath("notificationCreatedDate").type(STRING).description("공지 생성일. yyyy.MM.dd 형식."),
                fieldWithPath("notificationDetail").type(STRING).description("공지 상세 내용"));
    }

    private List<FieldDescriptor> notificationStatusFields() {
        return List.of(fieldWithPath("hasUnreadNotifications").type(BOOLEAN)
                .description("읽지 않은 알림이 하나라도 있는지 여부"));
    }

    private NotificationPageResponse notificationPageResponse() {
        return new NotificationPageResponse(true, List.of(
                new NotificationItem(102L, "https://image.websoso.kr/notification/feed.png",
                        "재혼 황후", "내 글에 댓글이 달렸어요.", "3분 전", false, false, 31L, null),
                new NotificationItem(101L, "https://image.websoso.kr/notification/novel.png",
                        "재혼 황후", "작품 연재가 재개되었어요.", "1시간 전", false, false, null, 7L),
                new NotificationItem(100L, "https://image.websoso.kr/notification/notice.png",
                        "서비스 점검 안내", "공지 내용을 확인해 주세요.", "2026.08.20", true, true, null, null)));
    }

    private MockHttpServletRequestBuilder notificationsRequest() {
        return get("/notifications")
                .queryParam("lastNotificationId", "0")
                .queryParam("size", "10");
    }

    private MockHttpServletRequestBuilder notificationDetailRequest() {
        return get("/notifications/{notificationId}", NOTIFICATION_ID);
    }

    private MockHttpServletRequestBuilder notificationReadRequest() {
        return patch("/notifications/{notificationId}/read-status", NOTIFICATION_ID);
    }

    private MockHttpServletRequestBuilder notificationLegacyReadRequest() {
        return post("/notifications/{notificationId}/read", NOTIFICATION_ID);
    }
}
