package org.websoso.WSSServer.collection.controller.collection;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import static org.websoso.WSSServer.user.exception.CustomBlockError.BLOCKED_USER_ACCESS;

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
import org.websoso.WSSServer.collection.application.CollectionFindApplication;
import org.websoso.WSSServer.collection.application.CollectionManagementApplication;
import org.websoso.WSSServer.collection.controller.CollectionController;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionsGetResponse;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code GET /users/{userId}/collections}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 *
 * <p>공개 범위와 차단 정책 자체는 Application 테스트가 확인한다. 여기서는 각 정책 위반이
 * 어떤 상태 코드와 본문으로 나가는지, 커서와 페이지 크기 파라미터가 어떻게 쓰이는지를 문서로 남긴다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionController.class)
class GetUserCollectionsDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long OWNER_ID = 7L;
    private static final String PATH = "/users/{userId}/collections";
    private static final String NEXT_CURSOR = "MjAyNS0wMS0wMlQwMDowMDowMHwxMg";

    private static final String TAG = "Collection";
    private static final String SUMMARY = "사용자별 컬렉션 목록 조회";
    private static final Schema RESPONSE_SCHEMA = Schema.schema("CollectionsGetResponse");

    private static final String DESCRIPTION = String.join("\n",
            "사용자가 만든 컬렉션을 최초 생성 시점 최신순으로 조회합니다.",
            "본인 목록에는 공개·비공개 컬렉션이 모두 포함되고, 다른 사용자의 목록에는 공개 컬렉션만 포함됩니다.",
            "마이페이지 미리보기는 별도 API 없이 이 API를 size=3으로 호출해 구성합니다.",
            "",
            "무한 스크롤은 커서로 이어 갑니다. 첫 요청은 cursor 없이 보내고, 이후에는 직전 응답의",
            "nextCursor를 그대로 넘깁니다. hasNext가 false면 nextCursor는 내려가지 않습니다.",
            "커서 값은 서버가 발급한 문자열이며 클라이언트가 만들거나 해석하지 않습니다.",
            "",
            "컬렉션 카드의 representativeNovel과 recentNovels 배열은 컬렉션 상세의 novels 배열과 같은",
            "작품 요약 구조(novelId, title, novelImage, author)입니다.",
            "",
            "representativeNovel은 카드 표지로 쓰는 대표 작품 하나이고, recentNovels는 최근 추가된 작품부터",
            "최대 5개입니다. 둘은 서로 독립적인 값이라 대표 작품이 최근에 추가된 작품이면 두 곳에 함께 내려갑니다.",
            "따라서 클라이언트는 recentNovels에서 대표 작품을 걸러 내지 않아도 되며, 걸러 낼지는 화면이 정합니다.",
            "아래 성공 응답 예시는 대표 작품이 recentNovels에 포함된 5개짜리 카드입니다.",
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
            errorLine(BLOCKED_USER_ACCESS) + " (어느 방향이든 차단 관계면 목록을 볼 수 없습니다.)",
            errorLine(USER_NOT_FOUND) + " (조회 대상 사용자나 토큰의 사용자 정보가 없을 때 반환합니다.)",
            "",
            "400과 401은 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CollectionManagementApplication collectionManagementApplication;

    @MockBean
    private CollectionFindApplication collectionFindApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
        given(collectionFindApplication.getUserCollections(any(User.class), eq(OWNER_ID), any(), anyInt()))
                .willReturn(collectionsResponse());
    }

    @DisplayName("컬렉션 목록 조회 성공(200) 응답을 문서화한다")
    @Test
    void documentGetUserCollectionsSuccess() throws Exception {
        mockMvc.perform(listRequest().with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.collectionsCount").value(12))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(NEXT_CURSOR))
                .andExpect(jsonPath("$.collections[0].recentNovels.length()").value(5))
                .andExpect(jsonPath("$.collections[0].representativeNovel.novelId").value(5))
                .andExpect(jsonPath("$.collections[0].representativeNovel.author").value("알파타르트"))
                .andExpect(jsonPath("$.collections[0].recentNovels[1].novelId").value(5))
                .andDo(document("user-collections-get",
                        resource(collection()
                                .queryParameters(queryParameters())
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseFields(responseFields())
                                .build())));
    }

    @DisplayName("마이페이지 미리보기는 같은 API를 size=3으로 호출한다")
    @Test
    void reusesSameApiForMyPagePreview() throws Exception {
        mockMvc.perform(get(PATH, OWNER_ID).param("size", "3").with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collectionsCount").value(12));
    }

    @DisplayName("커서와 크기를 생략해도 첫 페이지를 조회한다")
    @Test
    void queriesFirstPageWithoutParameters() throws Exception {
        mockMvc.perform(get(PATH, OWNER_ID).with(accessToken(USER_ID)))
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
                .andDo(document("user-collections-get-access-token-expired",
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
                .andDo(document("user-collections-get-invalid-token",
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
                .andDo(document("user-collections-get-wrong-token-type",
                        resource(collectionError().build())));
    }

    @DisplayName("서버가 발급하지 않은 커서 요청의 400 COLLECTION-007 응답을 문서화한다")
    @Test
    void documentGetWithInvalidCursor() throws Exception {
        givenListThrows(new CustomCollectionException(
                INVALID_COLLECTION_CURSOR, "collection cursor is not a value issued by this API"));

        documentListError(INVALID_COLLECTION_CURSOR, "user-collections-get-invalid-cursor");
    }

    @DisplayName("허용 범위를 벗어난 크기 요청의 400 COLLECTION-008 응답을 문서화한다")
    @Test
    void documentGetWithInvalidPageSize() throws Exception {
        givenListThrows(new CustomCollectionException(
                INVALID_COLLECTION_PAGE_SIZE, "collection page size must be between 1 and 100"));

        documentListError(INVALID_COLLECTION_PAGE_SIZE, "user-collections-get-invalid-size");
    }

    @DisplayName("차단 관계인 사용자의 목록 요청의 403 BLOCK-007 응답을 문서화한다")
    @Test
    void documentGetBlockedUserCollections() throws Exception {
        givenListThrows(new CustomBlockException(
                BLOCKED_USER_ACCESS, "cannot access content because either user has blocked the other"));

        documentListError(BLOCKED_USER_ACCESS, "user-collections-get-blocked-user");
    }

    @DisplayName("존재하지 않는 사용자의 목록 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentGetWithUnknownUser() throws Exception {
        givenListThrows(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        documentListError(USER_NOT_FOUND, "user-collections-get-user-not-found");
    }

    private void givenListThrows(RuntimeException exception) {
        given(collectionFindApplication.getUserCollections(any(User.class), eq(OWNER_ID), any(), anyInt()))
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
                .description(DESCRIPTION)
                .pathParameters(parameterWithName("userId")
                        .type(SimpleType.INTEGER)
                        .description("컬렉션을 조회할 사용자 ID"));
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
                        .description("조회자가 볼 수 있는 전체 컬렉션 개수. 이번 페이지 개수가 아니다."),
                fieldWithPath("hasNext").type(BOOLEAN).description("다음 페이지가 더 있는지 여부"),
                fieldWithPath("nextCursor").type(STRING).optional()
                        .description("다음 요청에 그대로 넘길 커서. 다음 페이지가 없으면 null이다."),
                fieldWithPath("collections").type(ARRAY)
                        .description("컬렉션 카드 배열. 최초 생성 시점 최신순이며 이번 페이지 분량만 담는다."),
                fieldWithPath("collections[].collectionId").type(NUMBER).description("컬렉션 ID"),
                fieldWithPath("collections[].collectionName").type(STRING).description("컬렉션 이름"),
                fieldWithPath("collections[].collectionDescription").type(STRING).optional()
                        .description("컬렉션 설명. 없으면 null이다."),
                fieldWithPath("collections[].isPublic").type(BOOLEAN).description("공개 여부"),
                fieldWithPath("collections[].novelCount").type(NUMBER)
                        .description("컬렉션에 포함된 전체 작품 수. recentNovels의 개수가 아니다."),
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
     * 성공 응답 예시는 최대치인 5개짜리 {@code recentNovels}로 만들고, 대표 작품을 그 안에 함께 넣는다.
     * 예시를 두 개만 담으면 최대 개수도, 대표 작품이 중복될 수 있다는 사실도 예시에서 읽히지 않는다.
     */
    private CollectionsGetResponse collectionsResponse() {
        CollectionNovelSummaryGetResponse representative = novelSummary(5L, "재혼 황후", "알파타르트");

        return CollectionsGetResponse.of(12L, true, NEXT_CURSOR, List.of(
                new CollectionPreviewGetResponse(
                        31L,
                        "취향 저격 로판",
                        "여주가 강한 로맨스 판타지 모음",
                        true,
                        24L,
                        representative,
                        List.of(
                                novelSummary(9L, "전지적 독자 시점", "싱숑"),
                                representative,
                                novelSummary(14L, "데뷔 못 하면 죽는 병 걸림", "백덕수"),
                                novelSummary(21L, "악녀는 두 번 산다", "한민트"),
                                novelSummary(28L, "폐하, 이만 저를 버려주세요", "여운")))));
    }

    private CollectionNovelSummaryGetResponse novelSummary(Long novelId, String title, String author) {
        return new CollectionNovelSummaryGetResponse(
                novelId, title, "https://image.websoso/novel/%d.png".formatted(novelId), author);
    }

    private MockHttpServletRequestBuilder listRequest() {
        return get(PATH, OWNER_ID)
                .param("cursor", NEXT_CURSOR)
                .param("size", "10");
    }
}
