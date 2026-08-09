package org.websoso.WSSServer.collection.controller.like;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_CURSOR;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_PAGE_SIZE;
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
import org.websoso.WSSServer.collection.application.CollectionLikeApplication;
import org.websoso.WSSServer.collection.application.CollectionLikeFindApplication;
import org.websoso.WSSServer.collection.controller.CollectionLikeController;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.LikedCollectionsGetResponse;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code GET /users/me/liked-collections}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 *
 * <p>공개 범위와 차단 정책 자체는 Application·Repository 테스트가 확인한다. 여기서는 응답 구조와
 * 커서·크기 파라미터가 어떻게 쓰이는지를 문서로 남긴다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionLikeController.class)
class GetLikedCollectionsDocsTest {

    private static final Long USER_ID = 42L;
    private static final String PATH = "/users/me/liked-collections";
    private static final String NEXT_CURSOR = "MjAyNS0wMS0wMlQwMDowMDowMHwxMg";

    private static final String TAG = "Collection";
    private static final String SUMMARY = "좋아요한 컬렉션 목록 조회";
    private static final Schema RESPONSE_SCHEMA = Schema.schema("LikedCollectionsGetResponse");

    private static final String DESCRIPTION = String.join("\n",
            "Access Token으로 인증한 사용자가 좋아요한 컬렉션을 좋아요한 시점 최신순으로 조회합니다.",
            "컬렉션을 만든 시점이 아니라 좋아요를 누른 시점이 정렬 기준이므로, 최근에 좋아요한 컬렉션이 앞에 옵니다.",
            "본인 목록만 조회하므로 경로에 사용자 ID를 받지 않습니다.",
            "",
            "좋아요 데이터는 컬렉션이 비공개로 바뀌어도 지우지 않고, 지금 볼 수 있는지는 조회 시점에 판단합니다.",
            "본인이 만든 비공개 컬렉션은 그대로 내려가고, 다른 사용자의 비공개 컬렉션과",
            "차단 관계 사용자의 컬렉션은 목록에서 숨깁니다. 숨겨진 컬렉션은 collectionsCount에도 세지 않습니다.",
            "",
            "무한 스크롤은 커서로 이어 갑니다. 첫 요청은 cursor 없이 보내고, 이후에는 직전 응답의",
            "nextCursor를 그대로 넘깁니다. hasNext가 false면 nextCursor는 내려가지 않습니다.",
            "커서 값은 서버가 발급한 문자열이며 클라이언트가 만들거나 해석하지 않습니다.",
            "이 API의 커서는 컬렉션이 아니라 좋아요를 가리키므로 사용자별 컬렉션 목록의 커서와 바꿔 쓸 수 없습니다.",
            "",
            "컬렉션 카드의 representativeNovel과 recentNovels 배열은 컬렉션 상세의 novels 배열과 같은",
            "작품 요약 구조(novelId, title, novelImage, author)입니다.",
            "목록에 담긴 컬렉션은 모두 조회자가 좋아요한 컬렉션이므로 카드에 좋아요 여부를 따로 내려보내지 않습니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(INVALID_COLLECTION_CURSOR),
            errorLine(INVALID_COLLECTION_PAGE_SIZE),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            "",
            "400과 401은 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    /**
     * 같은 Controller가 좋아요 등록·취소도 함께 의존하므로 슬라이스 테스트에서 대역으로 둔다.
     */
    @MockBean
    private CollectionLikeApplication collectionLikeApplication;

    @MockBean
    private CollectionLikeFindApplication collectionLikeFindApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
        given(collectionLikeFindApplication.getLikedCollections(any(User.class), any(), anyInt()))
                .willReturn(likedCollectionsResponse());
    }

    @DisplayName("좋아요한 컬렉션 목록 조회 성공(200) 응답을 문서화한다")
    @Test
    void documentGetLikedCollectionsSuccess() throws Exception {
        mockMvc.perform(listRequest().with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.collectionsCount").value(12))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(NEXT_CURSOR))
                .andExpect(jsonPath("$.collections[0].likeCount").value(128))
                .andExpect(jsonPath("$.collections[1].isPublic").value(false))
                .andDo(document("liked-collections-get",
                        resource(collection()
                                .queryParameters(queryParameters())
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseFields(responseFields())
                                .build())));
    }

    @DisplayName("커서와 크기를 생략해도 첫 페이지를 조회한다")
    @Test
    void queriesFirstPageWithoutParameters() throws Exception {
        mockMvc.perform(get(PATH).with(accessToken(USER_ID)))
                .andExpect(status().isOk());
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentGetWithExpiredAccessToken() throws Exception {
        mockMvc.perform(listRequest().with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("liked-collections-get-access-token-expired",
                        resource(collectionError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentGetWithInvalidToken() throws Exception {
        mockMvc.perform(listRequest())
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("liked-collections-get-invalid-token",
                        resource(collectionError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentGetWithWrongTokenType() throws Exception {
        mockMvc.perform(listRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("liked-collections-get-wrong-token-type",
                        resource(collectionError().build())));
    }

    @DisplayName("서버가 발급하지 않은 커서 요청의 400 COLLECTION-007 응답을 문서화한다")
    @Test
    void documentGetWithInvalidCursor() throws Exception {
        givenListThrows(new CustomCollectionException(
                INVALID_COLLECTION_CURSOR, "collection like cursor is not a value issued by this API"));

        documentListError(INVALID_COLLECTION_CURSOR, "liked-collections-get-invalid-cursor");
    }

    @DisplayName("허용 범위를 벗어난 크기 요청의 400 COLLECTION-008 응답을 문서화한다")
    @Test
    void documentGetWithInvalidPageSize() throws Exception {
        givenListThrows(new CustomCollectionException(
                INVALID_COLLECTION_PAGE_SIZE, "collection page size must be between 1 and 100"));

        documentListError(INVALID_COLLECTION_PAGE_SIZE, "liked-collections-get-invalid-size");
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentGetWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        documentListError(USER_NOT_FOUND, "liked-collections-get-user-not-found");
    }

    private void givenListThrows(RuntimeException exception) {
        given(collectionLikeFindApplication.getLikedCollections(any(User.class), any(), anyInt()))
                .willThrow(exception);
    }

    /**
     * 애플리케이션 예외로 정의된 실패 응답은 상태 코드와 본문이 모두 {@link ICustomError} 정의에서 정해진다.
     * 기대값을 테스트에 옮겨 적지 않고 정의에서 읽어 단언한다.
     */
    private void documentListError(ICustomError error, String documentIdentifier) throws Exception {
        mockMvc.perform(listRequest().with(accessToken(USER_ID)))
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
                .description(DESCRIPTION);
    }

    private List<ParameterDescriptorWithType> queryParameters() {
        return List.of(
                parameterWithName("cursor").type(SimpleType.STRING).optional()
                        .description("직전 응답의 nextCursor. 첫 페이지는 생략한다."),
                parameterWithName("size").type(SimpleType.INTEGER).optional()
                        .description("한 번에 가져올 컬렉션 수. 생략하면 10이고 1 이상 100 이하여야 한다."));
    }

    /**
     * 생성기가 중첩 객체에 독립 스키마 이름을 붙이지 않으므로(정책 12.3절), 값만으로는 읽히지 않는 계약은
     * 부모 필드 서술이 대신 담는다. 하위 필드는 값 자체만 짧게 설명한다.
     */
    private List<FieldDescriptor> responseFields() {
        return List.of(
                fieldWithPath("collectionsCount").type(NUMBER)
                        .description("조회자가 좋아요한 컬렉션 중 지금 볼 수 있는 전체 개수. 이번 페이지 개수가 아니다."),
                fieldWithPath("hasNext").type(BOOLEAN).description("다음 페이지가 더 있는지 여부"),
                fieldWithPath("nextCursor").type(STRING).optional()
                        .description("다음 요청에 그대로 넘길 커서. 다음 페이지가 없으면 null이다."),
                fieldWithPath("collections").type(ARRAY)
                        .description("컬렉션 카드 배열. 좋아요한 시점 최신순이며 이번 페이지 분량만 담는다."),
                fieldWithPath("collections[].collectionId").type(NUMBER).description("컬렉션 ID"),
                fieldWithPath("collections[].collectionName").type(STRING).description("컬렉션 이름"),
                fieldWithPath("collections[].collectionDescription").type(STRING).optional()
                        .description("컬렉션 설명. 없으면 null이다."),
                fieldWithPath("collections[].isPublic").type(BOOLEAN)
                        .description("공개 여부. false인 카드는 조회자 본인이 만든 비공개 컬렉션뿐이다."),
                fieldWithPath("collections[].novelCount").type(NUMBER)
                        .description("컬렉션에 포함된 전체 작품 수. recentNovels의 개수가 아니다."),
                fieldWithPath("collections[].likeCount").type(NUMBER)
                        .description("컬렉션이 받은 전체 좋아요 수. 조회자의 좋아요 하나가 아니다."),
                fieldWithPath("collections[].representativeNovel").type(OBJECT)
                        .description("카드 표지로 쓰는 대표 작품 하나. recentNovels와 독립적인 값이며 "
                                + "대표 작품이 최근 추가 작품이면 recentNovels에도 같은 작품이 함께 내려간다."),
                fieldWithPath("collections[].representativeNovel.novelId").type(NUMBER).description("작품 ID"),
                fieldWithPath("collections[].representativeNovel.title").type(STRING).description("작품 제목"),
                fieldWithPath("collections[].representativeNovel.novelImage").type(STRING)
                        .description("작품 표지 이미지 URL"),
                fieldWithPath("collections[].representativeNovel.author").type(STRING).description("작가"),
                fieldWithPath("collections[].recentNovels").type(ARRAY)
                        .description("카드 안 미리보기 줄. 최근 추가된 작품부터 최대 5개다. "
                                + "대표 작품을 제외하지 않으므로 representativeNovel과 같은 작품이 포함될 수 있다."),
                fieldWithPath("collections[].recentNovels[].novelId").type(NUMBER).description("작품 ID"),
                fieldWithPath("collections[].recentNovels[].title").type(STRING).description("작품 제목"),
                fieldWithPath("collections[].recentNovels[].novelImage").type(STRING)
                        .description("작품 표지 이미지 URL"),
                fieldWithPath("collections[].recentNovels[].author").type(STRING).description("작가"));
    }

    /**
     * 성공 응답 예시는 공개 컬렉션 카드와, 본인이 만든 비공개 컬렉션 카드를 함께 담는다.
     * 비공개 카드가 없는 예시로는 어떤 비공개 컬렉션이 이 목록에 남는지가 예시에서 읽히지 않는다.
     */
    private LikedCollectionsGetResponse likedCollectionsResponse() {
        CollectionNovelSummaryGetResponse representative = novelSummary(5L, "재혼 황후", "알파타르트");

        return LikedCollectionsGetResponse.of(12L, true, NEXT_CURSOR, List.of(
                new LikedCollectionPreviewGetResponse(
                        31L,
                        "취향 저격 로판",
                        "여주가 강한 로맨스 판타지 모음",
                        true,
                        24L,
                        128L,
                        representative,
                        List.of(
                                novelSummary(9L, "전지적 독자 시점", "싱숑"),
                                representative,
                                novelSummary(14L, "데뷔 못 하면 죽는 병 걸림", "백덕수"),
                                novelSummary(21L, "악녀는 두 번 산다", "한민트"),
                                novelSummary(28L, "폐하, 이만 저를 버려주세요", "여운"))),
                new LikedCollectionPreviewGetResponse(
                        44L,
                        "혼자 보는 정주행 목록",
                        null,
                        false,
                        3L,
                        1L,
                        novelSummary(9L, "전지적 독자 시점", "싱숑"),
                        List.of(novelSummary(9L, "전지적 독자 시점", "싱숑")))));
    }

    private CollectionNovelSummaryGetResponse novelSummary(Long novelId, String title, String author) {
        return new CollectionNovelSummaryGetResponse(
                novelId, title, "https://image.websoso/novel/%d.png".formatted(novelId), author);
    }

    private MockHttpServletRequestBuilder listRequest() {
        return get(PATH)
                .param("cursor", NEXT_CURSOR)
                .param("size", "10");
    }
}
