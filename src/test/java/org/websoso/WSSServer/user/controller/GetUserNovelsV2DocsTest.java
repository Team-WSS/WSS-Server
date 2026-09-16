package org.websoso.WSSServer.user.controller;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
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
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.websoso.WSSServer.dto.userNovel.UserNovelAndNovelGetResponse;
import org.websoso.WSSServer.dto.userNovel.UserNovelsV2GetResponse;
import org.websoso.WSSServer.library.service.UserNovelService;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

@AutoConfigureRestDocs
@AuthenticatedControllerTest(UserController.class)
class GetUserNovelsV2DocsTest {

    private static final long VISITOR_ID = 42L;
    private static final long OWNER_ID = 10034L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserNovelService userNovelService;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(VISITOR_ID)).willReturn(org.mockito.Mockito.mock(User.class));
    }

    @DisplayName("서재 작품의 작가명을 문서화한다")
    @Test
    void documentUserNovelsV2() throws Exception {
        given(userNovelService.getUserNovelsAndNovelsV2(
                any(User.class), eq(OWNER_ID), isNull(), eq(10), eq("created_desc"),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .willReturn(userNovelsResponse());

        mockMvc.perform(get("/users/{userId}/novels/v2", OWNER_ID)
                        .queryParam("size", "10")
                        .with(accessToken(VISITOR_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.userNovels[0].author").value("작가명"))
                .andDo(document("users-novels-v2-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("User Novel")
                                .summary("사용자 서재 작품 조회 V2")
                                .description("사용자의 서재 작품을 조회합니다. 각 작품은 제목과 함께 작가명을 반환합니다.")
                                .pathParameters(parameterWithName("userId").description("서재 소유자 사용자 ID"))
                                .queryParameters(parameterWithName("size").description("조회할 작품 개수"))
                                .responseSchema(Schema.schema("UserNovelsV2GetResponse"))
                                .responseFields(
                                        fieldWithPath("userNovelCount").type(NUMBER).description("필터에 해당하는 전체 작품 수"),
                                        fieldWithPath("isLoadable").type(BOOLEAN).description("다음 페이지 존재 여부"),
                                        fieldWithPath("nextCursor").type(STRING).description("다음 페이지 커서"),
                                        fieldWithPath("userNovels[]").type(ARRAY).description("서재 작품 목록"),
                                        fieldWithPath("userNovels[].userNovelId").type(NUMBER).description("서재 작품 ID"),
                                        fieldWithPath("userNovels[].novelId").type(NUMBER).description("작품 ID"),
                                        fieldWithPath("userNovels[].title").type(STRING).description("작품 제목"),
                                        fieldWithPath("userNovels[].author").type(STRING).description("작가명"),
                                        fieldWithPath("userNovels[].novelImage").type(STRING).description("작품 이미지 URL"),
                                        fieldWithPath("userNovels[].novelRating").type(NUMBER).description("작품 전체 평균 평점"),
                                        fieldWithPath("userNovels[].readStatus").type(STRING).description("독서 상태"),
                                        fieldWithPath("userNovels[].isInterest").type(BOOLEAN).description("관심 등록 여부"),
                                        fieldWithPath("userNovels[].userNovelRating").type(NUMBER)
                                                .description("서재 소유자가 직접 남긴 평점"),
                                        fieldWithPath("userNovels[].attractivePoints[]").type(ARRAY)
                                                .description("서재 소유자가 선택한 매력 포인트"),
                                        fieldWithPath("userNovels[].startDate").type(STRING).description("독서 시작일"),
                                        fieldWithPath("userNovels[].endDate").type(STRING).description("독서 종료일"),
                                        fieldWithPath("userNovels[].keywords[]").type(ARRAY)
                                                .description("서재 소유자가 선택한 키워드"),
                                        fieldWithPath("userNovels[].myFeeds[]").type(ARRAY)
                                                .description("서재 소유자가 이 작품에 작성한 피드 내용"))
                                .build())));
    }

    private UserNovelsV2GetResponse userNovelsResponse() {
        UserNovelAndNovelGetResponse userNovel = new UserNovelAndNovelGetResponse(
                1210L,
                2706L,
                "작품명",
                "작가명",
                "https://image.websoso.kr/novel/2706.jpg",
                4.3f,
                "WATCHING",
                true,
                3.0f,
                List.of("세계관"),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 10),
                List.of("성장물"),
                List.of("재미있게 읽는 중")
        );

        return new UserNovelsV2GetResponse(1L, true, "next-cursor", List.of(userNovel));
    }
}
