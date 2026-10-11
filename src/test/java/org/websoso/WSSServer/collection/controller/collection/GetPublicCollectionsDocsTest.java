package org.websoso.WSSServer.collection.controller.collection;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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
import static org.websoso.WSSServer.support.auth.TestBearerToken.tamperedAccessToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;
import static org.websoso.common.exception.CustomCommonError.REQUEST_VALUE_TYPE_MISMATCH;

import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import java.time.LocalDateTime;
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
import org.websoso.WSSServer.collection.controller.dto.CollectionOwnerGetResponse;
import org.websoso.WSSServer.collection.controller.dto.PublicCollectionPreviewGetResponse;
import org.websoso.WSSServer.collection.controller.dto.PublicCollectionsGetResponse;
import org.websoso.WSSServer.collection.domain.PublicCollectionCursor;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code GET /collections}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 *
 * <p>공개 범위·차단·커서 정책 자체는 Application 테스트가 확인한다. 여기서는 인증이 선택이라는 사실,
 * 각 정책 위반이 어떤 상태 코드와 본문으로 나가는지, 같은 경로의 생성과 다른 경로의 수정·삭제가 계속
 * 인증을 요구하는지를 확인한다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionController.class)
class GetPublicCollectionsDocsTest {

    private static final Long USER_ID = 42L;
    private static final String PATH = "/collections";
    private static final String NEXT_CURSOR = PublicCollectionCursor.of(
            LocalDateTime.of(2025, 1, 2, 0, 0), 12L).encode();
    private static final String SIZE_TYPE_MISMATCH_MESSAGE = "요청 값의 형식이 올바르지 않습니다: size";

    private static final String TAG = "Collection";
    private static final String SUMMARY = "전체 공개 컬렉션 목록 조회";
    private static final Schema RESPONSE_SCHEMA = Schema.schema("PublicCollectionsGetResponse");

    private static final String DESCRIPTION = String.join("\n",
            "모든 사용자의 공개 컬렉션을 최초 생성 시점 최신순으로 조회합니다. 생성 시각이 같으면 컬렉션 ID 내림차순입니다.",
            "수정·작품 재배치·좋아요로 정렬 위치가 바뀌지 않습니다.",
            "홈의 컬렉션 섹션은 첫 페이지를, 전체 컬렉션 화면은 첫 페이지와 이후 페이지를 이 API로 조회합니다.",
            "",
            "인증은 선택입니다. Authorization 헤더가 없거나, Bearer 형식이 아니거나, Bearer 뒤 토큰 값이 비어 있으면",
            "토큰이 없는 것으로 보고 비로그인 조회로 처리합니다.",
            "유효한 Access Token을 보내면 로그인 조회로 처리하고, 어느 방향이든 차단 관계인 작성자의 컬렉션을 목록에서 뺍니다.",
            "차단 때문에 목록 전체를 거부하지 않으며, 공개·차단 조건은 페이지 크기 제한 전에 적용합니다.",
            "Bearer 토큰 값을 보냈다면 유효해야 하므로 만료·위조·타입 오류는 비로그인 조회가 아니라 그대로 401입니다.",
            "",
            "공개 컬렉션만 포함합니다. 조회자 본인의 비공개 컬렉션도 이 목록에는 포함되지 않습니다.",
            "탈퇴한 사용자의 공개 컬렉션은 알 수 없는 사용자 프로필로 내려갑니다.",
            "공개 여부와 차단 관계는 요청마다 그 시점의 값으로 판단하며, 여러 페이지에 걸친 고정 스냅샷은 없습니다.",
            "새로고침은 cursor 없이 첫 페이지를 다시 조회합니다.",
            "",
            "무한 스크롤은 커서로 이어 갑니다. 첫 요청은 cursor 없이 보내고(빈 값·공백만 있는 값도 첫 페이지),",
            "이후에는 직전 응답의 nextCursor를 URL 인코딩해 그대로 넘깁니다. hasNext가 false면 nextCursor는 null입니다.",
            "커서 값은 서버가 발급한 문자열이며 클라이언트가 만들거나 해석하지 않습니다.",
            "사용자별 컬렉션 목록이나 좋아요한 컬렉션 목록의 커서는 이 API에서 쓸 수 없습니다.",
            "커서로 쓰던 컬렉션이 삭제돼도 다음 페이지를 조회할 수 있습니다.",
            "",
            "카드의 recentNovels는 컬렉션 상세의 novels 배열과 같은 작품 요약 구조(novelId, title, novelImage, author)이며,",
            "novelIds로 저장한 표시 순서 앞에서부터 최대 5개입니다. 대표 작품을 따로 앞에 넣거나 빼지 않습니다.",
            "실제 작품만 담으므로 5개보다 적을 수 있고, 부족한 표지 슬롯은 클라이언트가 기본 표지로 채웁니다.",
            "owner는 컬렉션 상세의 owner와 같은 구조(userId, nickname, avatarImage)입니다.",
            "좋아요 수, 공개 여부, 대표 작품, 전체 개수는 이 응답에 포함되지 않습니다.",
            "",
            "Try it out으로 로그인 상태를 재현하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공. 조회 가능한 컬렉션이 없으면 빈 배열입니다.",
            errorLine(INVALID_COLLECTION_CURSOR) + " (다른 목록의 커서를 포함합니다.)",
            errorLine(INVALID_COLLECTION_PAGE_SIZE),
            errorLine(REQUEST_VALUE_TYPE_MISMATCH, SIZE_TYPE_MISMATCH_MESSAGE) + " (size가 정수가 아닐 때)",
            errorLine(ACCESS_TOKEN_EXPIRED) + " (Bearer 토큰을 보냈을 때만 해당합니다.)",
            errorLine(INVALID_TOKEN) + " (Bearer 토큰 값이 위조·손상됐을 때입니다. 헤더가 없거나 Bearer 형식이 아니거나 "
                    + "토큰 값이 비어 있으면 오류가 아니라 비로그인 조회입니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
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
        given(collectionFindApplication.getPublicCollections(any(), any(), anyInt()))
                .willReturn(publicCollectionsResponse());
    }

    @DisplayName("전체 공개 컬렉션 목록 조회 성공(200) 응답을 문서화한다")
    @Test
    void documentGetPublicCollectionsSuccess() throws Exception {
        mockMvc.perform(listRequest().with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value(NEXT_CURSOR))
                .andExpect(jsonPath("$.collections[0].owner.userId").value(7))
                .andExpect(jsonPath("$.collections[0].owner.nickname").value("웹소소"))
                .andExpect(jsonPath("$.collections[0].owner.avatarImage")
                        .value("https://image.websoso/avatar/1.png"))
                .andExpect(jsonPath("$.collections[0].novelCount").value(24))
                .andExpect(jsonPath("$.collections[0].recentNovels.length()").value(5))
                .andExpect(jsonPath("$.collections[1].collectionDescription").isEmpty())
                .andExpect(jsonPath("$.collections[1].recentNovels.length()").value(1))
                .andExpect(jsonPath("$.collectionsCount").doesNotExist())
                .andExpect(jsonPath("$.collections[0].isPublic").doesNotExist())
                .andExpect(jsonPath("$.collections[0].representativeNovel").doesNotExist())
                .andExpect(jsonPath("$.collections[0].likeCount").doesNotExist())
                .andExpect(jsonPath("$.collections[0].isLiked").doesNotExist())
                .andDo(document("collections-list-get",
                        resource(collection()
                                .queryParameters(queryParameters())
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseFields(responseFields())
                                .build())));

        then(collectionFindApplication).should().getPublicCollections(any(User.class), eq(NEXT_CURSOR), eq(10));
    }

    @DisplayName("토큰 없이 요청하면 비로그인 조회로 공개 컬렉션 목록을 반환한다")
    @Test
    void readsPublicCollectionsWithoutToken() throws Exception {
        mockMvc.perform(listRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collections.length()").value(2));

        then(collectionFindApplication).should().getPublicCollections(isNull(), eq(NEXT_CURSOR), eq(10));
    }

    @DisplayName("Bearer 형식이 아닌 Authorization 헤더는 토큰 없음으로 보고 비로그인 조회로 처리한다")
    @Test
    void treatsNonBearerHeaderAsAnonymous() throws Exception {
        mockMvc.perform(listRequest().header(AUTHORIZATION, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isOk());

        then(collectionFindApplication).should().getPublicCollections(isNull(), eq(NEXT_CURSOR), eq(10));
    }

    @DisplayName("Bearer 뒤 토큰 값이 비어 있으면 토큰 없음으로 보고 비로그인 조회로 처리한다")
    @Test
    void treatsEmptyBearerTokenAsAnonymous() throws Exception {
        mockMvc.perform(listRequest().header(AUTHORIZATION, "Bearer "))
                .andExpect(status().isOk());

        then(collectionFindApplication).should().getPublicCollections(isNull(), eq(NEXT_CURSOR), eq(10));
    }

    @DisplayName("커서와 크기를 생략하면 커서 없이 기본 크기 10으로 첫 페이지를 조회한다")
    @Test
    void queriesFirstPageWithDefaultSize() throws Exception {
        mockMvc.perform(get(PATH))
                .andExpect(status().isOk());

        then(collectionFindApplication).should().getPublicCollections(isNull(), isNull(), eq(10));
    }

    @DisplayName("조회 가능한 컬렉션이 없으면 빈 배열과 커서 없음을 반환한다")
    @Test
    void returnsEmptyList() throws Exception {
        given(collectionFindApplication.getPublicCollections(any(), any(), anyInt()))
                .willReturn(PublicCollectionsGetResponse.of(false, null, List.of()));

        mockMvc.perform(get(PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").isEmpty())
                .andExpect(jsonPath("$.collections").isArray())
                .andExpect(jsonPath("$.collections.length()").value(0));
    }

    @DisplayName("서버가 발급하지 않은 커서 요청의 400 COLLECTION-007 응답을 문서화한다")
    @Test
    void documentGetWithInvalidCursor() throws Exception {
        givenListThrows(new CustomCollectionException(
                INVALID_COLLECTION_CURSOR, "public collection cursor is not a value issued by this API"));

        documentListError(INVALID_COLLECTION_CURSOR, "collections-list-get-invalid-cursor");
    }

    @DisplayName("허용 범위를 벗어난 크기 요청의 400 COLLECTION-008 응답을 문서화한다")
    @Test
    void documentGetWithInvalidPageSize() throws Exception {
        givenListThrows(new CustomCollectionException(
                INVALID_COLLECTION_PAGE_SIZE, "collection page size must be between 1 and 100"));

        documentListError(INVALID_COLLECTION_PAGE_SIZE, "collections-list-get-invalid-size");
    }

    @DisplayName("정수가 아닌 크기 요청의 400 COMMON-005 응답을 문서화한다")
    @Test
    void documentGetWithNonIntegerSize() throws Exception {
        mockMvc.perform(get(PATH).param("size", "ten"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(REQUEST_VALUE_TYPE_MISMATCH.getCode()))
                .andExpect(jsonPath("$.message").value(SIZE_TYPE_MISMATCH_MESSAGE))
                .andDo(document("collections-list-get-size-type-mismatch",
                        resource(collectionError().build())));

        then(collectionFindApplication).shouldHaveNoInteractions();
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentGetWithExpiredAccessToken() throws Exception {
        documentAuthError(listRequest().with(expiredAccessToken(USER_ID)), ACCESS_TOKEN_EXPIRED,
                "collections-list-get-access-token-expired");
    }

    @DisplayName("위조된 Access Token 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentGetWithInvalidToken() throws Exception {
        documentAuthError(listRequest().with(tamperedAccessToken(USER_ID)), INVALID_TOKEN,
                "collections-list-get-invalid-token");
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentGetWithWrongTokenType() throws Exception {
        documentAuthError(listRequest().with(refreshToken(USER_ID)), WRONG_TOKEN_TYPE,
                "collections-list-get-wrong-token-type");
    }

    @DisplayName("같은 경로의 컬렉션 생성은 여전히 토큰 없이 요청할 수 없다")
    @Test
    void createStillRequiresAuthentication() throws Exception {
        mockMvc.perform(post(PATH).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()));

        then(collectionManagementApplication).shouldHaveNoInteractions();
    }

    @DisplayName("컬렉션 수정과 삭제는 여전히 토큰 없이 요청할 수 없다")
    @Test
    void updateAndDeleteStillRequireAuthentication() throws Exception {
        mockMvc.perform(put("/collections/{collectionId}", 31L).contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/collections/{collectionId}", 31L))
                .andExpect(status().isUnauthorized());

        then(collectionManagementApplication).shouldHaveNoInteractions();
    }

    private void documentAuthError(MockHttpServletRequestBuilder request, ICustomError error,
                                   String documentIdentifier) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(error.getCode()))
                .andExpect(jsonPath("$.message").value(error.getDescription()))
                .andDo(document(documentIdentifier,
                        resource(collectionError().build())));

        then(collectionFindApplication).shouldHaveNoInteractions();
    }

    private void givenListThrows(RuntimeException exception) {
        given(collectionFindApplication.getPublicCollections(any(), any(), anyInt())).willThrow(exception);
    }

    /**
     * 애플리케이션 예외로 정의된 실패 응답은 상태 코드와 본문이 모두 {@link ICustomError} 정의에서 정해진다.
     * 기대값을 테스트에 옮겨 적지 않고 정의에서 읽어 단언한다.
     */
    private void documentListError(ICustomError error, String documentIdentifier) throws Exception {
        mockMvc.perform(listRequest())
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
                        .description("직전 응답의 nextCursor를 URL 인코딩한 값. 첫 페이지는 생략하며 "
                                + "빈 값이나 공백만 있는 값도 첫 페이지로 처리한다."),
                parameterWithName("size").type(SimpleType.INTEGER).optional()
                        .defaultValue(10)
                        .description("한 번에 가져올 컬렉션 수. 생략하면 10이고 1 이상 100 이하여야 한다."));
    }

    /**
     * 생성기가 중첩 객체에 독립 스키마 이름을 붙이지 않으므로(정책 12.3절), 값만으로는 읽히지 않는 계약은
     * 부모 필드 서술이 대신 담는다. 응답은 페이지 정보·컬렉션 카드·작성자/작품 요약 세 겹이다.
     */
    private List<FieldDescriptor> responseFields() {
        return List.of(
                fieldWithPath("hasNext").type(BOOLEAN).description("[페이지] 다음 페이지가 더 있는지 여부"),
                fieldWithPath("nextCursor").type(STRING).optional()
                        .description("[페이지] 다음 요청에 그대로 넘길 커서. hasNext가 false면 null이다."),
                fieldWithPath("collections").type(ARRAY)
                        .description("[페이지] 컬렉션 카드 배열. 최초 생성 시점 최신순(동률이면 ID 내림차순)이며 "
                                + "최대 size개다. 조회 가능한 컬렉션이 없으면 빈 배열이다."),
                fieldWithPath("collections[].collectionId").type(NUMBER).description("[카드] 컬렉션 ID"),
                fieldWithPath("collections[].collectionName").type(STRING).description("[카드] 컬렉션 이름"),
                fieldWithPath("collections[].collectionDescription").type(STRING).optional()
                        .description("[카드] 컬렉션 설명. 없으면 null이다."),
                fieldWithPath("collections[].novelCount").type(NUMBER)
                        .description("[카드] 컬렉션에 포함된 전체 작품 수. recentNovels의 개수가 아니다."),
                fieldWithPath("collections[].owner").type(OBJECT)
                        .description("[카드] 컬렉션을 만든 사용자. 컬렉션 상세의 owner와 같은 구조다. "
                                + "탈퇴한 사용자의 컬렉션은 알 수 없는 사용자 프로필로 내려간다."),
                fieldWithPath("collections[].owner.userId").type(NUMBER).description("[작성자] 사용자 ID"),
                fieldWithPath("collections[].owner.nickname").type(STRING).description("[작성자] 닉네임"),
                fieldWithPath("collections[].owner.avatarImage").type(STRING)
                        .description("[작성자] 아바타 이미지 URL. 아바타는 모든 사용자가 반드시 가지므로 항상 내려간다."),
                fieldWithPath("collections[].recentNovels").type(ARRAY)
                        .description("[카드] novelIds로 저장한 표시 순서 앞에서부터 최대 5개의 작품 요약. "
                                + "컬렉션 상세의 novels와 같은 구조이며 실제 작품만 담으므로 5개보다 적을 수 있다."),
                fieldWithPath("collections[].recentNovels[].novelId").type(NUMBER).description("[작품] 작품 ID"),
                fieldWithPath("collections[].recentNovels[].title").type(STRING).description("[작품] 작품 제목"),
                fieldWithPath("collections[].recentNovels[].novelImage").type(STRING)
                        .description("[작품] 작품 표지 이미지 URL"),
                fieldWithPath("collections[].recentNovels[].author").type(STRING).description("[작품] 작가"));
    }

    /**
     * 성공 응답 예시는 미리보기 최대치인 5개짜리 카드와, 설명이 없고 작품이 하나뿐인 카드를 함께 담는다.
     * 미리보기 개수의 상한과 부족한 슬롯을 서버가 채우지 않는다는 사실이 예시에서 함께 읽히게 한다.
     */
    private PublicCollectionsGetResponse publicCollectionsResponse() {
        return PublicCollectionsGetResponse.of(true, NEXT_CURSOR, List.of(
                new PublicCollectionPreviewGetResponse(
                        31L,
                        "취향 저격 로판",
                        "여주가 강한 로맨스 판타지 모음",
                        24L,
                        new CollectionOwnerGetResponse(7L, "웹소소", "https://image.websoso/avatar/1.png"),
                        List.of(
                                novelSummary(9L, "전지적 독자 시점", "싱숑"),
                                novelSummary(5L, "재혼 황후", "알파타르트"),
                                novelSummary(14L, "데뷔 못 하면 죽는 병 걸림", "백덕수"),
                                novelSummary(21L, "악녀는 두 번 산다", "한민트"),
                                novelSummary(28L, "폐하, 이만 저를 버려주세요", "여운"))),
                new PublicCollectionPreviewGetResponse(
                        12L,
                        "추천 소설",
                        null,
                        1L,
                        new CollectionOwnerGetResponse(45L, "콜렉터", "https://image.websoso/avatar/2.png"),
                        List.of(novelSummary(1001L, "작품 제목", "작가명")))));
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
