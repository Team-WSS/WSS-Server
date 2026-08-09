package org.websoso.WSSServer.collection.controller.collection;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.DUPLICATE_COLLECTION_NOVEL;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.INVALID_AUTHORIZED_COLLECTION;
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
import com.epages.restdocs.apispec.SimpleType;
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
import org.websoso.WSSServer.collection.controller.dto.CollectionUpdateRequest;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;
import org.websoso.WSSServer.exception.exception.CustomNovelException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;
import org.websoso.common.exception.ICustomError;

/**
 * {@code PUT /collections/{collectionId}}의 실제 응답을 REST Docs로 문서화하는 테스트.
 * 성공 응답과 이 호출 경로가 정의한 실패 응답을 요청으로 재현해 확인한 그대로 명세에 남긴다.
 *
 * <p>요청은 {@code RestDocumentationRequestBuilders}로 만들어야 URL 템플릿과 path parameter가
 * 명세에 기록된다. {@code Authorization} 헤더는 {@code requestHeaders}로 선언하지 않는다.
 */
@AutoConfigureRestDocs
@AuthenticatedControllerTest(CollectionController.class)
class PutCollectionDocsTest {

    private static final Long USER_ID = 42L;
    private static final Long COLLECTION_ID = 12L;
    private static final CollectionUpdateRequest UPDATE_REQUEST = new CollectionUpdateRequest(
            "취향 저격 로판", "여주가 강한 로맨스 판타지 모음", false, List.of(1L, 2L), 1L);

    private static final String PATH = "/collections/{collectionId}";
    private static final String TAG = "Collection";
    private static final String SUMMARY = "컬렉션 수정";
    private static final Schema UPDATE_REQUEST_SCHEMA = Schema.schema("CollectionUpdateRequest");
    private static final String MALFORMED_JSON_MESSAGE = "잘못된 JSON 형식입니다.";
    private static final String NULL_IS_PUBLIC_MESSAGE = "공개 여부는 null일 수 없습니다.";

    private static final String DESCRIPTION = String.join("\n",
            "Access Token으로 인증한 사용자가 소유한 컬렉션을 전체 교체 의미로 수정합니다.",
            "",
            "요청 본문은 수정 후의 컬렉션 전체 상태입니다. 생성과 달리 공개 여부를 생략할 수 없습니다.",
            "생략을 공개로 해석하면 필드를 빠뜨리는 것만으로 비공개 컬렉션이 공개로 바뀌기 때문입니다.",
            "",
            "Try it out으로 호출하려면 상단 Authorize에 실제 Access Token을 입력해야 합니다.",
            "",
            "이 API가 정의하는 응답은 다음과 같습니다.",
            "",
            "- 204 No Content — 성공. (응답 본문이 없습니다.)",
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
            errorLine(INVALID_AUTHORIZED_COLLECTION),
            errorLine(COLLECTION_NOT_FOUND),
            errorLine(USER_NOT_FOUND) + " (토큰은 유효하지만, 해당 사용자 정보가 없을 때 반환합니다.)",
            errorLine(NOVEL_NOT_FOUND) + " (요청한 작품 중 하나라도 존재하지 않을 때 반환합니다.)",
            "",
            "400, 401, 404는 상태 코드만으로 원인을 구분할 수 없습니다. 각 응답의 Examples에서 코드별 본문 확인이 필요합니다.");

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
    }

    @DisplayName("컬렉션 수정 성공(204, 본문 없음) 응답을 문서화한다")
    @Test
    void documentUpdateSuccess() throws Exception {
        mockMvc.perform(updateRequest(UPDATE_REQUEST).with(accessToken(USER_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("collections-update",
                        resource(collection()
                                .requestSchema(UPDATE_REQUEST_SCHEMA)
                                .requestFields(updateRequestFields())
                                .build())));
    }

    @DisplayName("만료된 Access Token 요청의 401 AUTH-000 응답을 문서화한다")
    @Test
    void documentUpdateWithExpiredAccessToken() throws Exception {
        mockMvc.perform(updateRequest(UPDATE_REQUEST).with(expiredAccessToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ACCESS_TOKEN_EXPIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED.getDescription()))
                .andDo(document("collections-update-access-token-expired",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("Authorization 헤더가 없는 요청의 401 AUTH-001 응답을 문서화한다")
    @Test
    void documentUpdateWithInvalidToken() throws Exception {
        mockMvc.perform(updateRequest(UPDATE_REQUEST))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getDescription()))
                .andDo(document("collections-update-invalid-token",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("Refresh Token으로 인증한 요청의 401 AUTH-003 응답을 문서화한다")
    @Test
    void documentUpdateWithWrongTokenType() throws Exception {
        mockMvc.perform(updateRequest(UPDATE_REQUEST).with(refreshToken(USER_ID)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(WRONG_TOKEN_TYPE.getCode()))
                .andExpect(jsonPath("$.message").value(WRONG_TOKEN_TYPE.getDescription()))
                .andDo(document("collections-update-wrong-token-type",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("본문 JSON 형식이 잘못된 요청의 400 BAD_REQUEST 응답을 문서화한다")
    @Test
    void documentUpdateWithMalformedJson() throws Exception {
        mockMvc.perform(put(PATH, COLLECTION_ID)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":")
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(MALFORMED_JSON_MESSAGE))
                .andDo(document("collections-update-malformed-json",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("공개 여부를 생략한 요청의 400 BAD_REQUEST 응답을 문서화한다")
    @Test
    void documentUpdateWithoutIsPublic() throws Exception {
        CollectionUpdateRequest withoutIsPublic = new CollectionUpdateRequest(
                "취향 저격 로판", "여주가 강한 로맨스 판타지 모음", null, List.of(1L, 2L), 1L);

        mockMvc.perform(updateRequest(withoutIsPublic).with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value(NULL_IS_PUBLIC_MESSAGE))
                .andDo(document("collections-update-invalid-request-field",
                        withoutRequestBody(),
                        resource(collectionError().build())));
    }

    @DisplayName("토큰은 유효하지만 사용자가 없는 요청의 404 USER-006 응답을 문서화한다")
    @Test
    void documentUpdateWithUnknownUser() throws Exception {
        given(userService.getUserOrException(USER_ID))
                .willThrow(new CustomUserException(USER_NOT_FOUND, "user with the given id was not found"));

        documentUpdateError(USER_NOT_FOUND, "collections-update-user-not-found");
    }

    @DisplayName("존재하지 않는 컬렉션 요청의 404 COLLECTION-001 응답을 문서화한다")
    @Test
    void documentUpdateWithUnknownCollection() throws Exception {
        givenUpdateThrows(new CustomCollectionException(
                COLLECTION_NOT_FOUND, "collection with the given id is not found"));

        documentUpdateError(COLLECTION_NOT_FOUND, "collections-update-collection-not-found");
    }

    @DisplayName("소유자가 아닌 사용자의 요청의 403 COLLECTION-005 응답을 문서화한다")
    @Test
    void documentUpdateWithoutOwnership() throws Exception {
        givenUpdateThrows(new CustomCollectionException(
                INVALID_AUTHORIZED_COLLECTION, "only the owner can modify or delete the collection"));

        documentUpdateError(INVALID_AUTHORIZED_COLLECTION, "collections-update-not-owner");
    }

    @DisplayName("포함 작품 수가 허용 범위를 벗어난 요청의 400 COLLECTION-002 응답을 문서화한다")
    @Test
    void documentUpdateWithInvalidNovelCount() throws Exception {
        givenUpdateThrows(new CustomCollectionException(
                INVALID_COLLECTION_NOVEL_COUNT, "collection must contain between 1 and 100 novels"));

        documentUpdateError(INVALID_COLLECTION_NOVEL_COUNT, "collections-update-invalid-novel-count");
    }

    @DisplayName("같은 작품을 중복으로 포함한 요청의 400 COLLECTION-003 응답을 문서화한다")
    @Test
    void documentUpdateWithDuplicateNovel() throws Exception {
        givenUpdateThrows(new CustomCollectionException(
                DUPLICATE_COLLECTION_NOVEL, "collection cannot contain the same novel more than once"));

        documentUpdateError(DUPLICATE_COLLECTION_NOVEL, "collections-update-duplicate-novel");
    }

    @DisplayName("대표 작품이 포함 작품에 없는 요청의 400 COLLECTION-004 응답을 문서화한다")
    @Test
    void documentUpdateWithRepresentativeNovelNotIncluded() throws Exception {
        givenUpdateThrows(new CustomCollectionException(REPRESENTATIVE_NOVEL_NOT_INCLUDED,
                "representative novel must be one of the novels included in the collection"));

        documentUpdateError(REPRESENTATIVE_NOVEL_NOT_INCLUDED,
                "collections-update-representative-novel-not-included");
    }

    @DisplayName("존재하지 않는 작품을 포함한 요청의 404 NOVEL-001 응답을 문서화한다")
    @Test
    void documentUpdateWithUnknownNovel() throws Exception {
        givenUpdateThrows(new CustomNovelException(NOVEL_NOT_FOUND, "novel with id 2 is not found"));

        documentUpdateError(NOVEL_NOT_FOUND, "collections-update-novel-not-found");
    }

    private void givenUpdateThrows(RuntimeException exception) {
        willThrow(exception).given(collectionManagementApplication)
                .update(any(User.class), eq(COLLECTION_ID), any(CollectionUpdateRequest.class));
    }

    /**
     * 애플리케이션 예외로 정의된 실패 응답은 상태 코드와 본문이 모두 {@link ICustomError} 정의에서 정해진다.
     * 기대값을 테스트에 옮겨 적지 않고 정의에서 읽어 단언한다.
     */
    private void documentUpdateError(ICustomError error, String documentIdentifier) throws Exception {
        mockMvc.perform(updateRequest(UPDATE_REQUEST).with(accessToken(USER_ID)))
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
                .description(DESCRIPTION)
                .pathParameters(parameterWithName("collectionId")
                        .type(SimpleType.INTEGER)
                        .description("수정할 컬렉션 ID"));
    }

    private List<FieldDescriptor> updateRequestFields() {
        return List.of(
                fieldWithPath("name").type(STRING)
                        .description("컬렉션 이름. 필수이며 공백일 수 없고 20자를 초과할 수 없다."),
                fieldWithPath("description").type(STRING).optional()
                        .description("컬렉션 설명. 생략할 수 있고 60자를 초과할 수 없다."),
                fieldWithPath("isPublic").type(BOOLEAN)
                        .description("공개 여부. 필수이며 생략할 수 없다."),
                fieldWithPath("novelIds").type(ARRAY)
                        .attributes(key("itemsType").value("number"))
                        .description("컬렉션에 포함할 작품 ID 목록. 필수이며 1개 이상 100개 이하이고 중복될 수 없다. "
                                + "배열 순서가 그대로 표시 순서로 저장되므로, 포함 작품을 바꾸지 않고 순서만 재배치해 보내도 된다."),
                fieldWithPath("representativeNovelId").type(NUMBER)
                        .description("대표 작품 ID. 필수이며 novelIds에 포함된 작품이어야 한다."));
    }

    private MockHttpServletRequestBuilder updateRequest(CollectionUpdateRequest request) throws Exception {
        return put(PATH, COLLECTION_ID)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
    }
}
