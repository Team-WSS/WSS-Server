package org.websoso.WSSServer.collection.controller.collection;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.PRIVATE_COLLECTION_ACCESS;
import static org.websoso.WSSServer.domain.common.SortCriteria.OLD;
import static org.websoso.WSSServer.domain.common.SortCriteria.RECENT;
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
import org.websoso.WSSServer.collection.controller.dto.CollectionGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionNovelSummaryGetResponse;
import org.websoso.WSSServer.collection.controller.dto.CollectionOwnerGetResponse;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.exception.CustomBlockException;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code GET /collections/{collectionId}}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 *
 * <p>인증이 선택인 API이므로 토큰 없는 요청이 401이 아니라 비로그인 조회로 처리되는 것까지 확인한다.
 * 토큰을 보냈다면 형식은 유효해야 하므로 만료·위조·타입 오류는 그대로 401이다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionController.class)
class GetCollectionDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long COLLECTION_ID = 31L;
    private static final String PATH = "/collections/{collectionId}";

    private static final String TAG = "Collection";
    private static final String SUMMARY = "컬렉션 상세 조회";
    private static final Schema RESPONSE_SCHEMA = Schema.schema("CollectionGetResponse");

    private static final String DESCRIPTION = String.join("\n",
            "컬렉션과 그 컬렉션에 담긴 작품의 화면 표시 정보를 함께 조회합니다.",
            "공유 링크로 들어온 비로그인 사용자도 공개 컬렉션은 볼 수 있으므로 인증은 선택입니다.",
            "Authorization 헤더 없이 호출하면 비로그인 조회로 처리되고, 비공개 컬렉션에는 접근할 수 없습니다.",
            "",
            "포함 작품은 컬렉션에 추가된 시점을 기준으로 정렬합니다.",
            "sortCriteria=RECENT이면 최근에 추가한 작품부터, OLD이면 먼저 추가한 작품부터 반환합니다.",
            "포함 작품에는 페이지네이션이 없습니다. 컬렉션의 작품 수가 최대 100개로 제한되므로 한 번에 모두 반환합니다.",
            "",
            "novels 배열은 컬렉션 목록의 representativeNovel·recentNovels 배열과 같은 작품 요약 구조",
            "(novelId, title, novelImage, author)입니다. 완결 여부와 평점은 이 응답에 포함되지 않으며",
            "필요하면 작품 상세 API에서 조회합니다.",
            "",
            "owner.avatarImage는 항상 내려갑니다. 아바타는 모든 사용자가 반드시 가지는 값입니다.",
            "",
            "Try it out으로 로그인 상태를 재현하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            errorLine(ACCESS_TOKEN_EXPIRED) + " (토큰을 보냈을 때만 해당합니다.)",
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류를 포함하며, 헤더가 아예 없으면 비로그인 조회입니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(PRIVATE_COLLECTION_ACCESS),
            errorLine(BLOCKED_USER_ACCESS) + " (어느 방향이든 차단 관계면 공유 링크로도 볼 수 없습니다.)",
            errorLine(COLLECTION_NOT_FOUND),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            "",
            "401과 403은 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

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
        given(collectionFindApplication.getCollection(any(), eq(COLLECTION_ID), any()))
                .willReturn(collectionResponse(true));
    }

    @DisplayName("컬렉션 상세 조회 성공(200) 응답을 문서화한다")
    @Test
    void documentGetCollectionSuccess() throws Exception {
        mockMvc.perform(detailRequest().with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.collectionId").value(COLLECTION_ID))
                .andExpect(jsonPath("$.isMyCollection").value(true))
                .andExpect(jsonPath("$.novels.length()").value(2))
                .andExpect(jsonPath("$.owner.userId").value(7))
                .andExpect(jsonPath("$.owner.nickname").value("웹소소"))
                .andExpect(jsonPath("$.owner.avatarImage").value("https://image.websoso/avatar/1.png"))
                .andExpect(jsonPath("$.novels[0].author").value("싱숑"))
                .andExpect(jsonPath("$.novels[0].isCompleted").doesNotExist())
                .andExpect(jsonPath("$.novels[0].novelRating").doesNotExist())
                .andExpect(jsonPath("$.novels[0].novelRatingCount").doesNotExist())
                .andDo(document("collections-get",
                        resource(collection()
                                .queryParameters(queryParameters())
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseFields(responseFields())
                                .build())));
    }

    @DisplayName("토큰 없이 요청하면 비로그인 조회로 공개 컬렉션 상세를 반환한다")
    @Test
    void readsPublicCollectionWithoutToken() throws Exception {
        given(collectionFindApplication.getCollection(isNull(), eq(COLLECTION_ID), any()))
                .willReturn(collectionResponse(false));

        mockMvc.perform(detailRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isMyCollection").value(false));
    }

    @DisplayName("정렬 기준을 생략해도 상세를 조회한다")
    @Test
    void readsCollectionWithoutSortCriteria() throws Exception {
        mockMvc.perform(get(PATH, COLLECTION_ID))
                .andExpect(status().isOk());
    }

    @DisplayName("오래된순 정렬 기준을 그대로 전달한다")
    @Test
    void passesOldestSortCriteria() throws Exception {
        mockMvc.perform(get(PATH, COLLECTION_ID).param("sortCriteria", OLD.name()))
                .andExpect(status().isOk());

        then(collectionFindApplication).should()
                .getCollection(isNull(), eq(COLLECTION_ID), eq(OLD));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentGetWithExpiredAccessToken() throws Exception {
        mockMvc.perform(detailRequest().with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("collections-get-access-token-expired",
                        resource(collectionError().build())));
    }

    @DisplayName("위조된 Access Token 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentGetWithInvalidToken() throws Exception {
        mockMvc.perform(detailRequest().with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("collections-get-invalid-token",
                        resource(collectionError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentGetWithWrongTokenType() throws Exception {
        mockMvc.perform(detailRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("collections-get-wrong-token-type",
                        resource(collectionError().build())));
    }

    @DisplayName("소유자가 아닌 사용자의 비공개 컬렉션 요청의 403 COLLECTION-006 응답을 문서화한다")
    @Test
    void documentGetPrivateCollection() throws Exception {
        givenDetailThrows(new CustomCollectionException(
                PRIVATE_COLLECTION_ACCESS, "only the owner of the collection can read a private collection"));

        documentDetailError(PRIVATE_COLLECTION_ACCESS, "collections-get-private-collection");
    }

    @DisplayName("차단 관계인 사용자의 컬렉션 요청의 403 BLOCK-007 응답을 문서화한다")
    @Test
    void documentGetBlockedUserCollection() throws Exception {
        givenDetailThrows(new CustomBlockException(
                BLOCKED_USER_ACCESS, "cannot access content because either user has blocked the other"));

        documentDetailError(BLOCKED_USER_ACCESS, "collections-get-blocked-user");
    }

    @DisplayName("존재하지 않는 컬렉션 요청의 404 COLLECTION-001 응답을 문서화한다")
    @Test
    void documentGetUnknownCollection() throws Exception {
        givenDetailThrows(new CustomCollectionException(
                COLLECTION_NOT_FOUND, "collection with the given id is not found"));

        documentDetailError(COLLECTION_NOT_FOUND, "collections-get-collection-not-found");
    }

    private void givenDetailThrows(RuntimeException exception) {
        given(collectionFindApplication.getCollection(any(), eq(COLLECTION_ID), any())).willThrow(exception);
    }

    /**
     * 애플리케이션 예외로 정의된 실패 응답은 상태 코드와 본문이 모두 {@link ICustomError} 정의에서 정해진다.
     * 기대값을 테스트에 옮겨 적지 않고 정의에서 읽어 단언한다.
     */
    private void documentDetailError(ICustomError error, String documentIdentifier) throws Exception {
        mockMvc.perform(detailRequest().with(accessToken(USER_ID)))
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
                        .description("조회할 컬렉션 ID"));
    }

    /**
     * Swagger UI에서 직접 입력 대신 값을 고르게 하려면 생성 명세의 파라미터 스키마에 enum이 있어야 한다.
     * restdocs-api-spec은 {@code enumValues} 속성을 읽어 스키마 enum으로 옮기므로 허용 값을 그 속성으로 넘긴다.
     * 생략하면 서버가 최근 추가순으로 정렬하므로 같은 값을 스키마 default로 남긴다.
     */
    private List<ParameterDescriptorWithType> queryParameters() {
        return List.of(parameterWithName("sortCriteria").type(SimpleType.STRING).optional()
                .defaultValue(RECENT.name())
                .attributes(key("enumValues").value(List.of(RECENT.name(), OLD.name())))
                .description("포함 작품 정렬 기준. RECENT는 최근 추가순, OLD는 오래된 추가순이며 생략하면 RECENT다."));
    }

    /**
     * 생성기가 중첩 객체에 독립 스키마 이름을 붙이지 않으므로(정책 12.3절), 값만으로는 읽히지 않는 계약은
     * 부모 필드 서술이 대신 담는다. 하위 필드는 값 자체만 짧게 설명한다.
     */
    private List<FieldDescriptor> responseFields() {
        return List.of(
                fieldWithPath("collectionId").type(NUMBER).description("컬렉션 ID"),
                fieldWithPath("collectionName").type(STRING).description("컬렉션 이름"),
                fieldWithPath("collectionDescription").type(STRING).optional()
                        .description("컬렉션 설명. 없으면 null이다."),
                fieldWithPath("isPublic").type(BOOLEAN).description("공개 여부"),
                fieldWithPath("isMyCollection").type(BOOLEAN)
                        .description("조회자가 소유자인지 여부. 비로그인 조회는 항상 false다."),
                fieldWithPath("owner").type(OBJECT)
                        .description("컬렉션을 만든 사용자. 공유 링크로 들어온 비로그인 조회자에게도 내려간다."),
                fieldWithPath("owner.userId").type(NUMBER).description("소유자 사용자 ID"),
                fieldWithPath("owner.nickname").type(STRING).description("소유자 닉네임"),
                fieldWithPath("owner.avatarImage").type(STRING)
                        .description("소유자 아바타 이미지 URL. 아바타는 모든 사용자가 반드시 가지므로 항상 내려간다."),
                fieldWithPath("representativeNovelId").type(NUMBER)
                        .description("대표 작품 ID. novels에 포함된 작품 중 하나다."),
                fieldWithPath("novelCount").type(NUMBER)
                        .description("컬렉션에 포함된 작품 수. novels의 길이와 같다."),
                fieldWithPath("novels").type(ARRAY)
                        .description("포함 작품 전체. 요청한 추가 시점 정렬 기준을 따르며 페이지네이션은 없다."),
                fieldWithPath("novels[].novelId").type(NUMBER).description("작품 ID"),
                fieldWithPath("novels[].title").type(STRING).description("작품 제목"),
                fieldWithPath("novels[].novelImage").type(STRING).description("작품 표지 이미지 URL"),
                fieldWithPath("novels[].author").type(STRING).description("작가"));
    }

    private CollectionGetResponse collectionResponse(boolean isMyCollection) {
        return new CollectionGetResponse(
                COLLECTION_ID,
                "취향 저격 로판",
                "여주가 강한 로맨스 판타지 모음",
                true,
                isMyCollection,
                new CollectionOwnerGetResponse(7L, "웹소소", "https://image.websoso/avatar/1.png"),
                5L,
                2,
                List.of(
                        new CollectionNovelSummaryGetResponse(9L, "전지적 독자 시점",
                                "https://image.websoso/novel/9.png", "싱숑"),
                        new CollectionNovelSummaryGetResponse(5L, "재혼 황후",
                                "https://image.websoso/novel/5.png", "알파타르트")));
    }

    private MockHttpServletRequestBuilder detailRequest() {
        return get(PATH, COLLECTION_ID).param("sortCriteria", RECENT.name());
    }
}
