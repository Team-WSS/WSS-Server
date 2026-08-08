package org.websoso.WSSServer.collection.controller.collection;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.DUPLICATE_COLLECTION_NOVEL;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_COLLECTION_NOVEL_COUNT;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.REPRESENTATIVE_NOVEL_NOT_INCLUDED;
import static org.websoso.WSSServer.exception.error.CustomAuthError.ACCESS_TOKEN_EXPIRED;
import static org.websoso.WSSServer.exception.error.CustomAuthError.INVALID_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomAuthError.WRONG_TOKEN_TYPE;
import static org.websoso.WSSServer.exception.error.CustomNovelError.NOVEL_NOT_FOUND;
import static org.websoso.WSSServer.exception.error.CustomUserError.USER_NOT_FOUND;
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
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateRequest;
import org.websoso.WSSServer.collection.controller.dto.CollectionCreateResponse;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code POST /collections}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 * 여기서 만든 snippet이 {@code ./gradlew apiDocs}에서 OpenAPI 3 명세로 변환된다.
 *
 * <p>{@code Authorization} 헤더는 {@code requestHeaders}로 선언하지 않는다. restdocs-api-spec이
 * 요청의 Bearer 토큰을 보고 security requirement를 만들기 때문에, 함께 선언하면 같은 헤더가
 * Authorize와 요청 파라미터로 두 번 나오고 테스트가 발급한 토큰이 명세에 그대로 박힌다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionController.class)
class PostCollectionDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long COLLECTION_ID = 12L;
    private static final CollectionCreateRequest CREATE_REQUEST = new CollectionCreateRequest(
            "취향 저격 로판", "여주가 강한 로맨스 판타지 모음", true, List.of(1L, 2L), 1L);

    private static final String TAG = "Collection";
    private static final String SUMMARY = "컬렉션 생성";
    private static final Schema CREATE_REQUEST_SCHEMA = Schema.schema("CollectionCreateRequest");
    private static final Schema CREATE_RESPONSE_SCHEMA = Schema.schema("CollectionCreateResponse");
    private static final String MALFORMED_JSON_MESSAGE = "잘못된 JSON 형식입니다.";
    private static final String BLANK_NAME_MESSAGE = "컬렉션 이름은 비어 있거나, 공백일 수 없습니다.";

    private static final String DESCRIPTION = String.join("\n",
            "Access Token으로 인증한 사용자의 컬렉션을 만듭니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 201 Created — 성공. (생성된 컬렉션 ID를 반환합니다.)",
            errorLine(ACCESS_TOKEN_EXPIRED),
            errorLine(INVALID_TOKEN) + " (헤더 형식 오류 또는 누락을 포함합니다.)",
            errorLine(WRONG_TOKEN_TYPE),
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), MALFORMED_JSON_MESSAGE)
                    + " (본문이 JSON으로 읽히지 않을 때 반환합니다.)",
            errorLine(BAD_REQUEST, BAD_REQUEST.name(), "검증에 실패한 첫 번째 요청 필드의 메시지")
                    + " (요청 본문 필드 검증에 실패했을 때 반환하며, 메시지는 실패한 필드에 따라 다릅니다.)",
            errorLine(INVALID_COLLECTION_NOVEL_COUNT),
            errorLine(DUPLICATE_COLLECTION_NOVEL),
            errorLine(REPRESENTATIVE_NOVEL_NOT_INCLUDED),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            errorLine(NOVEL_NOT_FOUND) + " (요청한 작품 중 하나라도 존재하지 않을 때 반환합니다.)",
            "",
            "400과 401은 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        given(collectionManagementApplication.create(any(User.class), any(CollectionCreateRequest.class)))
                .willReturn(new CollectionCreateResponse(COLLECTION_ID));
    }

    @DisplayName("컬렉션 생성 성공(201, 생성된 컬렉션 ID) 응답을 문서화한다")
    @Test
    void documentCreateSuccess() throws Exception {
        mockMvc.perform(createRequest(CREATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.collectionId").value(COLLECTION_ID))
                .andDo(document("collections-create",
                        resource(collection()
                                .requestSchema(CREATE_REQUEST_SCHEMA)
                                .requestFields(createRequestFields())
                                .responseSchema(CREATE_RESPONSE_SCHEMA)
                                .responseFields(createResponseFields())
                                .build())));
    }

    /**
     * {@code isPublic}이 선택 필드라는 계약을 확인한다. 생략해도 요청 검증을 통과하고,
     * 생략을 공개(true)로 해석하는 것은 컬렉션 도메인의 책임이라 이 계층에서는 관여하지 않는다.
     * 성공 응답은 위에서 이미 문서화했으므로 여기서는 snippet을 만들지 않는다.
     */
    @DisplayName("isPublic을 생략한 요청도 검증을 통과해 201로 생성된다")
    @Test
    void createsCollectionWhenIsPublicOmitted() throws Exception {
        String bodyWithoutIsPublic = """
                {"name":"취향 저격 로판","description":"여주가 강한 로맨스 판타지 모음",\
                "novelIds":[1,2],"representativeNovelId":1}""";

        mockMvc.perform(post("/collections")
                        .contentType(APPLICATION_JSON)
                        .content(bodyWithoutIsPublic)
                        .with(accessToken(USER_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.collectionId").value(COLLECTION_ID));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentCreateWithExpiredAccessToken() throws Exception {
        mockMvc.perform(createRequest(CREATE_REQUEST).with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("collections-create-access-token-expired",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentCreateWithInvalidToken() throws Exception {
        mockMvc.perform(createRequest(CREATE_REQUEST))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("collections-create-invalid-token",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentCreateWithWrongTokenType() throws Exception {
        mockMvc.perform(createRequest(CREATE_REQUEST).with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("collections-create-wrong-token-type",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("본문 JSON 형식이 잘못된 요청의 400 BAD_REQUEST 응답을 문서화한다")
    @Test
    void documentCreateWithMalformedJson() throws Exception {
        mockMvc.perform(post("/collections")
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(MALFORMED_JSON_MESSAGE))
                .andDo(document("collections-create-malformed-json",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("이름이 공백인 요청의 400 BAD_REQUEST 응답을 문서화한다")
    @Test
    void documentCreateWithBlankName() throws Exception {
        CollectionCreateRequest blankName =
                new CollectionCreateRequest(" ", "여주가 강한 로맨스 판타지 모음", true, List.of(1L, 2L), 1L);

        mockMvc.perform(createRequest(blankName).with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(BLANK_NAME_MESSAGE))
                .andDo(document("collections-create-invalid-request-field",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentCreateWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        mockMvc.perform(createRequest(CREATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND.getDescription()))
                .andDo(document("collections-create-user-not-found",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("포함 작품 수가 허용 범위를 벗어난 요청의 400 COLLECTION-002 응답을 문서화한다")
    @Test
    void documentCreateWithInvalidNovelCount() throws Exception {
        givenCreateThrows(new CustomCollectionException(
                INVALID_COLLECTION_NOVEL_COUNT, "collection must contain between 1 and 100 novels"));

        documentCreateError(INVALID_COLLECTION_NOVEL_COUNT, "collections-create-invalid-novel-count");
    }

    @DisplayName("같은 작품을 중복으로 포함한 요청의 400 COLLECTION-003 응답을 문서화한다")
    @Test
    void documentCreateWithDuplicateNovel() throws Exception {
        givenCreateThrows(new CustomCollectionException(
                DUPLICATE_COLLECTION_NOVEL, "collection cannot contain the same novel more than once"));

        documentCreateError(DUPLICATE_COLLECTION_NOVEL, "collections-create-duplicate-novel");
    }

    @DisplayName("대표 작품이 포함 작품에 없는 요청의 400 COLLECTION-004 응답을 문서화한다")
    @Test
    void documentCreateWithRepresentativeNovelNotIncluded() throws Exception {
        givenCreateThrows(new CustomCollectionException(REPRESENTATIVE_NOVEL_NOT_INCLUDED,
                "representative novel must be one of the novels included in the collection"));

        documentCreateError(REPRESENTATIVE_NOVEL_NOT_INCLUDED,
                "collections-create-representative-novel-not-included");
    }

    @DisplayName("존재하지 않는 작품을 포함한 요청의 404 NOVEL-001 응답을 문서화한다")
    @Test
    void documentCreateWithUnknownNovel() throws Exception {
        givenCreateThrows(new CustomNovelException(NOVEL_NOT_FOUND, "novel with id 2 is not found"));

        documentCreateError(NOVEL_NOT_FOUND, "collections-create-novel-not-found");
    }

    private void givenCreateThrows(RuntimeException exception) {
        given(collectionManagementApplication.create(any(User.class), any(CollectionCreateRequest.class)))
                .willThrow(exception);
    }

    /**
     * 애플리케이션 예외로 정의된 실패 응답은 상태 코드와 본문이 모두 {@link ICustomError} 정의에서 정해진다.
     * 기대값을 테스트에 옮겨 적지 않고 정의에서 읽어 단언한다.
     */
    private void documentCreateError(ICustomError error, String documentIdentifier) throws Exception {
        mockMvc.perform(createRequest(CREATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().is(error.getStatusCode().value()))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(error.getCode()))
                .andExpect(jsonPath("$.message").value(error.getDescription()))
                .andDo(document(documentIdentifier,
                        withoutRequestBody(),
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

    private List<FieldDescriptor> createRequestFields() {
        return List.of(
                fieldWithPath("name").type(STRING)
                        .description("컬렉션 이름. 필수이며 공백일 수 없고 20자를 초과할 수 없다."),
                fieldWithPath("description").type(STRING).optional()
                        .description("컬렉션 설명. 생략할 수 있고 60자를 초과할 수 없다."),
                fieldWithPath("isPublic").type(BOOLEAN).optional()
                        .description("공개 여부. 생략할 수 있고 생략하면 공개(true)로 생성한다."),
                fieldWithPath("novelIds").type(ARRAY)
                        .attributes(key("itemsType").value("number"))
                        .description("컬렉션에 포함할 작품 ID 목록. 필수이며 1개 이상 100개 이하이고 중복될 수 없다."),
                fieldWithPath("representativeNovelId").type(NUMBER)
                        .description("대표 작품 ID. 필수이며 novelIds에 포함된 작품이어야 한다."));
    }

    private List<FieldDescriptor> createResponseFields() {
        return List.of(fieldWithPath("collectionId").type(NUMBER).description("생성된 컬렉션 ID"));
    }

    private MockHttpServletRequestBuilder createRequest(CollectionCreateRequest request) throws Exception {
        return post("/collections")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
    }
}
