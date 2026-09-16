package org.websoso.WSSServer.controller.novel;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
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
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.expiredAccessToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.refreshToken;
import static org.websoso.WSSServer.support.auth.TestBearerToken.tamperedAccessToken;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.ERROR_RESULT_SCHEMA;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorLine;
import static org.websoso.WSSServer.support.docs.ErrorResponseDocumentation.errorResultFields;
import static org.websoso.common.exception.CustomCommonError.MISSING_REQUEST_PARAMETER;
import static org.websoso.common.exception.CustomCommonError.REQUEST_VALUE_TYPE_MISMATCH;

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
import org.websoso.WSSServer.application.SearchNovelApplication;
import org.websoso.WSSServer.controller.NovelController;
import org.websoso.WSSServer.dto.novel.NovelSummaryResponse;
import org.websoso.WSSServer.dto.novel.SearchedNovelsResponse;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code GET /novels}의 실제 응답을 문서화하는 REST Docs 테스트.
 * 검색어로 작품을 찾는 API이며, 인증은 선택이다.
 *
 * <p>이 API는 조회와 함께 최근 검색어 저장이라는 부수 효과를 가진다. 저장 여부를 요청이 정하는
 * {@code recordRecentSearch}의 기본값과 분기 동작을 계약으로 함께 검증한다. 검색 화면이 아닌 곳에서
 * 같은 API로 작품을 고를 때 사용자가 검색한 적 없는 단어가 최근 검색어로 남지 않게 하는 계약이다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(NovelController.class)
class GetNovelsDocsTest {

    private static final Long USER_ID = 42L;
    private static final String TAG = "Novel";
    private static final String SUMMARY = "작품 검색";
    private static final String QUERY = "재혼 황후";
    private static final Schema RESPONSE_SCHEMA = Schema.schema("SearchedNovelsResponse");
    private static final String PAGE_REQUIRED_MESSAGE = "필수 요청 파라미터가 없습니다: page";
    private static final String RECORD_RECENT_SEARCH_MISMATCH_MESSAGE =
            "요청 값의 형식이 올바르지 않습니다: recordRecentSearch";

    private static final String DESCRIPTION = String.join("\n",
            "검색어로 작품을 찾습니다. 작품명과 작가명을 함께 찾습니다.",
            "",
            "검색어는 공백과 한글·영문·숫자 외의 문자를 제거한 뒤 사용합니다.",
            "제거하고 나면 비는 검색어는 조회하지 않고 빈 목록(resultCount 0, isLoadable false)을 반환합니다.",
            "",
            "로그인 상태로 호출하면 이번 검색어를 최근 검색어로 저장합니다.",
            "recordRecentSearch를 false로 보내면 저장하지 않습니다. 생략하면 true로 동작해 저장합니다.",
            "검색 화면이 아닌 곳(작품 연결, 컬렉션에 작품 담기 등)에서 이 API로 작품을 고를 때 false를 보냅니다.",
            "false로 보내면 인증 여부, 검색 결과 개수, 페이지와 관계없이 저장하지 않습니다.",
            "true이거나 생략했더라도 비로그인 호출과 정규화 후 빈 검색어는 저장하지 않습니다.",
            "검색 결과가 0건이어도 저장하며, 같은 검색어의 다음 페이지 조회도 저장 시각을 갱신합니다.",
            "저장은 조회 응답과 무관한 비동기 처리라 실패해도 검색 결과에는 영향을 주지 않습니다.",
            "",
            "인증은 선택입니다. Authorization 헤더 없이 호출하면 비로그인 조회로 처리합니다.",
            "로그인 상태를 Try it out으로 확인하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 200 OK — 성공.",
            errorLine(MISSING_REQUEST_PARAMETER, PAGE_REQUIRED_MESSAGE) + " (size를 빼면 size 이름으로 반환합니다.)",
            errorLine(REQUEST_VALUE_TYPE_MISMATCH, RECORD_RECENT_SEARCH_MISMATCH_MESSAGE)
                    + " (page, size, recordRecentSearch를 선언한 형식으로 변환하지 못했을 때 반환합니다.)",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN)
                    + " (Authorization 헤더 형식이 잘못됐거나 토큰이 손상됐을 때 반환합니다."
                    + " 헤더가 아예 없으면 오류가 아니라 비로그인 조회입니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            "",
            "400과 401은 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SearchNovelApplication searchNovelApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("검색어로 작품을 찾는 200 응답을 문서화한다")
    @Test
    void documentSearchNovelsSuccess() throws Exception {
        given(searchNovelApplication.searchNovels(any(), anyString(), anyInt(), anyInt(), anyBoolean()))
                .willReturn(searchedNovels());

        mockMvc.perform(searchRequest().queryParam("recordRecentSearch", "true").with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.resultCount").value(2))
                .andExpect(jsonPath("$.isLoadable").value(true))
                .andExpect(jsonPath("$.novels[0].novelId").value(1))
                .andExpect(jsonPath("$.novels[1].novelId").value(2))
                .andDo(document("novels-search",
                        resource(searchNovels()
                                .responseSchema(RESPONSE_SCHEMA)
                                .responseFields(searchNovelsResponseFields())
                                .build())));

        then(searchNovelApplication).should().searchNovels(any(User.class), eq(QUERY), eq(0), eq(10), eq(true));
    }

    // 기존 클라이언트는 파라미터를 보내지 않는다. 생략이 곧 저장이라는 기본값 계약을 고정한다.
    @DisplayName("recordRecentSearch를 생략하면 최근 검색어를 저장하도록 위임한다")
    @Test
    void delegatesWithRecordRecentSearchTrueWhenParameterIsOmitted() throws Exception {
        given(searchNovelApplication.searchNovels(any(), anyString(), anyInt(), anyInt(), anyBoolean()))
                .willReturn(searchedNovels());

        mockMvc.perform(searchRequest().with(accessToken(USER_ID)))
                .andExpect(status().isOk());

        then(searchNovelApplication).should().searchNovels(any(User.class), eq(QUERY), eq(0), eq(10), eq(true));
    }

    @DisplayName("recordRecentSearch가 false면 최근 검색어를 저장하지 않도록 위임한다")
    @Test
    void delegatesWithRecordRecentSearchFalse() throws Exception {
        given(searchNovelApplication.searchNovels(any(), anyString(), anyInt(), anyInt(), anyBoolean()))
                .willReturn(searchedNovels());

        mockMvc.perform(searchRequest().queryParam("recordRecentSearch", "false").with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount").value(2))
                .andExpect(jsonPath("$.novels[0].novelId").value(1));

        then(searchNovelApplication).should().searchNovels(any(User.class), eq(QUERY), eq(0), eq(10), eq(false));
    }

    // 비로그인 호출은 인증 오류가 아니다. 사용자 없이 그대로 조회로 위임되는지 확인한다.
    @DisplayName("Authorization 헤더가 없는 검색은 비로그인 조회로 위임한다")
    @Test
    void delegatesWithoutUserWhenRequestIsAnonymous() throws Exception {
        given(searchNovelApplication.searchNovels(any(), anyString(), anyInt(), anyInt(), anyBoolean()))
                .willReturn(searchedNovels());

        mockMvc.perform(searchRequest())
                .andExpect(status().isOk());

        then(searchNovelApplication).should().searchNovels(null, QUERY, 0, 10, true);
    }

    @DisplayName("페이지를 보내지 않은 요청의 400 COMMON-004 응답을 문서화한다")
    @Test
    void documentSearchNovelsWithoutPage() throws Exception {
        mockMvc.perform(get("/novels")
                        .queryParam("query", QUERY)
                        .queryParam("size", "10")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(MISSING_REQUEST_PARAMETER.getCode()))
                .andExpect(jsonPath("$.message").value(PAGE_REQUIRED_MESSAGE))
                .andDo(document("novels-search-page-required",
                        resource(withoutQueryParameters()
                                .responseSchema(ERROR_RESULT_SCHEMA)
                                .responseFields(errorResultFields())
                                .build())));
    }

    @DisplayName("boolean이 아닌 recordRecentSearch 요청의 400 COMMON-005 응답을 문서화한다")
    @Test
    void documentSearchNovelsWithNonBooleanRecordRecentSearch() throws Exception {
        mockMvc.perform(searchRequest().queryParam("recordRecentSearch", "maybe").with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(REQUEST_VALUE_TYPE_MISMATCH.getCode()))
                .andExpect(jsonPath("$.message").value(RECORD_RECENT_SEARCH_MISMATCH_MESSAGE))
                .andDo(document("novels-search-record-recent-search-mismatch",
                        resource(searchNovelsError().build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentSearchNovelsWithExpiredAccessToken() throws Exception {
        mockMvc.perform(searchRequest().with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("novels-search-access-token-expired",
                        resource(searchNovelsError().build())));
    }

    @DisplayName("손상된 Access Token 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentSearchNovelsWithInvalidToken() throws Exception {
        mockMvc.perform(searchRequest().with(tamperedAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("novels-search-invalid-token",
                        resource(searchNovelsError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentSearchNovelsWithWrongTokenType() throws Exception {
        mockMvc.perform(searchRequest().with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("novels-search-wrong-token-type",
                        resource(searchNovelsError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentSearchNovelsWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(searchRequest().with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("novels-search-user-not-found",
                        resource(searchNovelsError().build())));
    }

    private ResourceSnippetParametersBuilder searchNovelsError() {
        return searchNovels()
                .responseSchema(ERROR_RESULT_SCHEMA)
                .responseFields(errorResultFields());
    }

    /**
     * 필수 파라미터 누락을 문서화할 때 쓴다. REST Docs는 선택이 아닌 query parameter를 선언해 두고
     * 요청에 없으면 snippet 생성을 실패시키므로, 이 경우에만 파라미터 서술을 붙이지 않는다.
     */
    private ResourceSnippetParametersBuilder withoutQueryParameters() {
        return ResourceSnippetParameters.builder()
                .tag(TAG)
                .summary(SUMMARY)
                .description(DESCRIPTION);
    }

    private ResourceSnippetParametersBuilder searchNovels() {
        return withoutQueryParameters()
                .queryParameters(
                        parameterWithName("query").optional()
                                .type(SimpleType.STRING)
                                .description("검색할 작품명 또는 작가명"),
                        parameterWithName("page")
                                .type(SimpleType.INTEGER)
                                .description("조회할 페이지. 0부터 시작한다."),
                        parameterWithName("size")
                                .type(SimpleType.INTEGER)
                                .description("한 페이지에서 조회할 작품 개수"),
                        parameterWithName("recordRecentSearch").optional()
                                .type(SimpleType.BOOLEAN)
                                .defaultValue(true)
                                .description("이번 검색어를 최근 검색어로 저장할지 여부."
                                        + " 생략하면 true다. false면 저장하지 않는다."));
    }

    private List<FieldDescriptor> searchNovelsResponseFields() {
        return List.of(
                fieldWithPath("resultCount").type(NUMBER)
                        .description("[페이지 정보] 검색어에 해당하는 작품의 전체 개수. 이번 페이지의 개수가 아니다."),
                fieldWithPath("isLoadable").type(BOOLEAN).description("[페이지 정보] 다음 페이지가 있는지 여부"),
                fieldWithPath("novels[]").type(ARRAY).description("[작품 카드] 이번 페이지의 검색 결과"),
                fieldWithPath("novels[].novelId").type(NUMBER).description("[작품 카드] 작품 ID"),
                fieldWithPath("novels[].novelImage").type(STRING).description("[작품 카드] 작품 이미지 URL"),
                fieldWithPath("novels[].title").type(STRING).description("[작품 카드] 작품 제목"),
                fieldWithPath("novels[].author").type(STRING).description("[작품 카드] 작가명"),
                fieldWithPath("novels[].interestCount").type(NUMBER).description("[작품 카드] 관심 등록 수"),
                fieldWithPath("novels[].novelRating").type(NUMBER)
                        .description("[작품 카드] 평균 별점. 소수 첫째 자리까지 반올림한 값이며, 평가가 없으면 0.0이다."),
                fieldWithPath("novels[].novelRatingCount").type(NUMBER).description("[작품 카드] 별점을 남긴 사용자 수"));
    }

    private SearchedNovelsResponse searchedNovels() {
        return SearchedNovelsResponse.of(
                List.of(
                        new NovelSummaryResponse(
                                1L, "https://image.websoso.kr/novel/1.jpg", "재혼 황후", "알파타르트", 120L, 4.3f, 58L),
                        new NovelSummaryResponse(
                                2L, "https://image.websoso.kr/novel/2.jpg", "재혼 황후 외전", "알파타르트", 12L, 0.0f, 0L)),
                2L,
                true);
    }

    private MockHttpServletRequestBuilder searchRequest() {
        return get("/novels")
                .queryParam("query", QUERY)
                .queryParam("page", "0")
                .queryParam("size", "10");
    }
}
