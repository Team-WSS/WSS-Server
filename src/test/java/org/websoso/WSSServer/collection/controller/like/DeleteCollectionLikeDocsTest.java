package org.websoso.WSSServer.collection.controller.like;

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
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.PRIVATE_COLLECTION_ACCESS;
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
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCKED_USER_ACCESS;

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
import org.websoso.WSSServer.collection.application.CollectionLikeApplication;
import org.websoso.WSSServer.collection.application.CollectionLikeFindApplication;
import org.websoso.WSSServer.collection.controller.CollectionLikeController;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code DELETE /collections/{collectionId}/likes}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionLikeController.class)
class DeleteCollectionLikeDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long COLLECTION_ID = 12L;

    private static final String PATH = "/collections/{collectionId}/likes";
    private static final String TAG = "Collection";
    private static final String SUMMARY = "컬렉션 좋아요 취소";

    private static final String DESCRIPTION = String.join("\n",
            "Access Token으로 인증한 사용자가 컬렉션에 누른 좋아요를 취소합니다.",
            "",
            "같은 요청을 반복해도 결과는 같습니다. 좋아요하지 않은 컬렉션의 취소 요청도 오류가 아니라 204를 반환하므로,",
            "클라이언트는 현재 좋아요 상태를 몰라도 \"좋아요하지 않은 상태로 만든다\"는 의도만 보내면 됩니다.",
            "",
            "좋아요 등록과 같은 접근 정책을 적용합니다. 볼 수 없는 컬렉션은 좋아요를 취소할 수도 없습니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 204 No Content — 성공. (응답 본문이 없습니다.)",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(PRIVATE_COLLECTION_ACCESS) + " (다른 사용자의 비공개 컬렉션은 좋아요를 취소할 수도 없습니다.)",
            errorLine(BLOCKED_USER_ACCESS) + " (어느 방향이든 차단 관계면 좋아요를 취소할 수 없습니다.)",
            errorLine(COLLECTION_NOT_FOUND),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            "",
            "401, 403, 404는 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CollectionLikeApplication collectionLikeApplication;

    /**
     * 같은 Controller가 좋아요한 컬렉션 목록 조회도 함께 의존하므로 슬라이스 테스트에서 대역으로 둔다.
     */
    @MockBean
    private CollectionLikeFindApplication collectionLikeFindApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("컬렉션 좋아요 취소 성공(204, 본문 없음) 응답을 문서화한다")
    @Test
    void documentUnlikeSuccess() throws Exception {
        mockMvc.perform(unlikeRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("collection-likes-delete",
                        resource(collection().build())));
    }

    @DisplayName("좋아요하지 않은 컬렉션의 취소 요청도 같은 성공 응답을 준다")
    @Test
    void repeatedUnlikeReturnsSameResponse() throws Exception {
        mockMvc.perform(unlikeRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNoContent());

        mockMvc.perform(unlikeRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentUnlikeWithExpiredAccessToken() throws Exception {
        mockMvc.perform(unlikeRequest().with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("collection-likes-delete-access-token-expired",
                        resource(collectionError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentUnlikeWithInvalidToken() throws Exception {
        mockMvc.perform(unlikeRequest())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("collection-likes-delete-invalid-token",
                        resource(collectionError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentUnlikeWithWrongTokenType() throws Exception {
        mockMvc.perform(unlikeRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("collection-likes-delete-wrong-token-type",
                        resource(collectionError().build())));
    }

    @DisplayName("다른 사용자의 비공개 컬렉션 요청의 403 COLLECTION-006 응답을 문서화한다")
    @Test
    void documentUnlikeOnPrivateCollection() throws Exception {
        givenUnlikeThrows(new CustomCollectionException(
                PRIVATE_COLLECTION_ACCESS, "only the owner of the collection can access a private collection"));

        documentUnlikeError(PRIVATE_COLLECTION_ACCESS, "collection-likes-delete-private-collection");
    }

    @DisplayName("차단 관계인 사용자의 컬렉션 요청의 403 BLOCK-007 응답을 문서화한다")
    @Test
    void documentUnlikeOnBlockedUserCollection() throws Exception {
        givenUnlikeThrows(new CustomBlockException(
                BLOCKED_USER_ACCESS, "cannot access content because either user has blocked the other"));

        documentUnlikeError(BLOCKED_USER_ACCESS, "collection-likes-delete-blocked-user");
    }

    @DisplayName("존재하지 않는 컬렉션 요청의 404 COLLECTION-001 응답을 문서화한다")
    @Test
    void documentUnlikeOnUnknownCollection() throws Exception {
        givenUnlikeThrows(new CustomCollectionException(
                COLLECTION_NOT_FOUND, "collection with the given id is not found"));

        documentUnlikeError(COLLECTION_NOT_FOUND, "collection-likes-delete-collection-not-found");
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentUnlikeWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        documentUnlikeError(USER_NOT_FOUND, "collection-likes-delete-user-not-found");
    }

    private void givenUnlikeThrows(RuntimeException exception) {
        willThrow(exception).given(collectionLikeApplication).delete(any(User.class), eq(COLLECTION_ID));
    }

    /**
     * 애플리케이션 예외로 정의된 실패 응답은 상태 코드와 본문이 모두 {@link ICustomError} 정의에서 정해진다.
     * 기대값을 테스트에 옮겨 적지 않고 정의에서 읽어 단언한다.
     */
    private void documentUnlikeError(ICustomError error, String documentIdentifier) throws Exception {
        mockMvc.perform(unlikeRequest().with(accessToken(USER_ID)))
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
                        .description("좋아요를 취소할 컬렉션 ID"));
    }

    private MockHttpServletRequestBuilder unlikeRequest() {
        return delete(PATH, COLLECTION_ID);
    }
}
