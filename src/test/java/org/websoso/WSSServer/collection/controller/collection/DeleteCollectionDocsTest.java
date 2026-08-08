package org.websoso.WSSServer.collection.controller.collection;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_AUTHORIZED_COLLECTION;
import static org.websoso.WSSServer.exception.error.CustomAuthError.ACCESS_TOKEN_EXPIRED;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomAuthError.WRONG_TOKEN_TYPE;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.SimpleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.websoso.WSSServer.collection.application.CollectionFindApplication;
import org.websoso.WSSServer.collection.application.CollectionManagementApplication;
import org.websoso.WSSServer.collection.controller.CollectionController;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code DELETE /collections/{collectionId}}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 *
 * <p>요청 본문이 없는 API이므로 본문이 JSON으로 읽히지 않을 때의 400과 요청 필드 검증 실패는
 * 이 호출 경로에 존재하지 않는다. 포함 작품을 다시 조회하지 않으므로 작품 오류도 도달할 수 없다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionController.class)
class DeleteCollectionDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long COLLECTION_ID = 12L;

    private static final String PATH = "/collections/{collectionId}";
    private static final String TAG = "Collection";
    private static final String SUMMARY = "컬렉션 삭제";

    private static final String DESCRIPTION = String.join("\n",
            "Access Token으로 인증한 사용자가 소유한 컬렉션을 삭제합니다.",
            "컬렉션에 어떤 작품이 담겨 있었는지에 대한 정보만 함께 지워지고, 작품 자체는 삭제되지 않습니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 204 No Content — 성공. (응답 본문이 없습니다.)",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(INVALID_AUTHORIZED_COLLECTION),
            errorLine(COLLECTION_NOT_FOUND),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            "",
            "401과 404는 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CollectionManagementApplication collectionManagementApplication;

    /**
     * 같은 Controller가 조회 유스케이스도 함께 의존하므로 슬라이스 테스트에서 대역으로 둔다.
     */
    @MockBean
    private CollectionFindApplication collectionFindApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("컬렉션 삭제 성공(204, 본문 없음) 응답을 문서화한다")
    @Test
    void documentDeleteSuccess() throws Exception {
        mockMvc.perform(deleteRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("collections-delete",
                        resource(collection().build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentDeleteWithExpiredAccessToken() throws Exception {
        mockMvc.perform(deleteRequest().with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("collections-delete-access-token-expired",
                        resource(collectionError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentDeleteWithInvalidToken() throws Exception {
        mockMvc.perform(deleteRequest())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("collections-delete-invalid-token",
                        resource(collectionError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentDeleteWithWrongTokenType() throws Exception {
        mockMvc.perform(deleteRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("collections-delete-wrong-token-type",
                        resource(collectionError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentDeleteWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        documentDeleteError(USER_NOT_FOUND, "collections-delete-user-not-found");
    }

    @DisplayName("존재하지 않는 컬렉션 요청의 404 COLLECTION-001 응답을 문서화한다")
    @Test
    void documentDeleteWithUnknownCollection() throws Exception {
        givenDeleteThrows(new CustomCollectionException(
                COLLECTION_NOT_FOUND, "collection with the given id is not found"));

        documentDeleteError(COLLECTION_NOT_FOUND, "collections-delete-collection-not-found");
    }

    @DisplayName("소유자가 아닌 사용자의 요청의 403 COLLECTION-005 응답을 문서화한다")
    @Test
    void documentDeleteWithoutOwnership() throws Exception {
        givenDeleteThrows(new CustomCollectionException(
                INVALID_AUTHORIZED_COLLECTION, "only the owner can modify or delete the collection"));

        documentDeleteError(INVALID_AUTHORIZED_COLLECTION, "collections-delete-not-owner");
    }

    private void givenDeleteThrows(RuntimeException exception) {
        willThrow(exception).given(collectionManagementApplication).delete(any(User.class), eq(COLLECTION_ID));
    }

    /**
     * 애플리케이션 예외로 정의된 실패 응답은 상태 코드와 본문이 모두 {@link ICustomError} 정의에서 정해진다.
     * 기대값을 테스트에 옮겨 적지 않고 정의에서 읽어 단언한다.
     */
    private void documentDeleteError(ICustomError error, String documentIdentifier) throws Exception {
        mockMvc.perform(deleteRequest().with(accessToken(USER_ID)))
                .andExpect(status().is(error.getStatusCode().value()))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(error.getCode()))
                .andExpect(jsonPath("$.message").value(error.getDescription()))
                .andDo(document(documentIdentifier,
                        resource(collectionError().build())));
    }

    private ResourceSnippetParametersBuilder collectionError() {
        return collection()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    private ResourceSnippetParametersBuilder collection() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION)
                .pathParameters(parameterWithName("collectionId")
                        .type(SimpleType.INTEGER)
                        .description("삭제할 컬렉션 ID"));
    }

    private MockHttpServletRequestBuilder deleteRequest() {
        return delete(PATH, COLLECTION_ID);
    }
}
