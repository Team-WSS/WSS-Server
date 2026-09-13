package org.websoso.WSSServer.controller.novel;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.websoso.WSSServer.application.SearchNovelApplication;
import org.websoso.WSSServer.controller.NovelController;
import org.websoso.WSSServer.dto.novel.NovelGetResponseBasic;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

@AutoConfigureRestDocs
@AuthenticatedControllerTest(NovelController.class)
class GetNovelBasicDocsTest {

    private static final long USER_ID = 42L;
    private static final long NOVEL_ID = 2706L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SearchNovelApplication searchNovelApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(org.mockito.Mockito.mock(User.class));
    }

    @DisplayName("작품 기본 정보의 작품 통계와 사용자 평점을 문서화한다")
    @Test
    void documentNovelBasicInfo() throws Exception {
        given(searchNovelApplication.getNovelInfoBasic(any(User.class), org.mockito.ArgumentMatchers.eq(NOVEL_ID)))
                .willReturn(novelBasicResponse());

        mockMvc.perform(get("/novels/{novelId}", NOVEL_ID).with(accessToken(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.novelRating").value(4.3))
                .andExpect(jsonPath("$.novelRatingCount").value(8))
                .andExpect(jsonPath("$.userNovelRating").value(3.0))
                .andDo(document("novels-basic-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Novel")
                                .summary("작품 기본 정보 조회")
                                .description("작품 기본 정보와 작품 전체 평점 통계, 현재 사용자의 서재 정보를 조회합니다."
                                        + " novelRating은 작품 전체 통계이고 userNovelRating은 현재 사용자가 남긴 평점입니다.")
                                .pathParameters(parameterWithName("novelId").description("작품 ID"))
                                .responseSchema(Schema.schema("NovelGetResponseBasic"))
                                .responseFields(
                                        fieldWithPath("userNovelId").type(NUMBER).description("현재 사용자의 서재 작품 ID"),
                                        fieldWithPath("novelTitle").type(STRING).description("작품 제목"),
                                        fieldWithPath("novelImage").type(STRING).description("작품 이미지 URL"),
                                        fieldWithPath("novelGenres").type(STRING).description("작품 장르"),
                                        fieldWithPath("novelGenreImage").type(STRING).description("대표 장르 이미지 URL"),
                                        fieldWithPath("isNovelCompleted").type(BOOLEAN).description("완결 여부"),
                                        fieldWithPath("author").type(STRING).description("작가명"),
                                        fieldWithPath("interestCount").type(NUMBER).description("관심 등록 수"),
                                        fieldWithPath("novelRating").type(NUMBER)
                                                .description("작품 전체 평균 평점. novel_statistics 기준이며 소수 첫째 자리까지 반올림합니다."),
                                        fieldWithPath("novelRatingCount").type(NUMBER)
                                                .description("작품에 평점을 남긴 사용자 수. novel_statistics 기준입니다."),
                                        fieldWithPath("feedCount").type(NUMBER).description("작품 피드 수"),
                                        fieldWithPath("userNovelRating").type(NUMBER)
                                                .description("현재 사용자가 작품에 남긴 평점. 작품 전체 평균 평점과 별개의 값입니다."),
                                        fieldWithPath("readStatus").type(STRING).description("현재 사용자의 독서 상태"),
                                        fieldWithPath("startDate").type(STRING).description("독서 시작일"),
                                        fieldWithPath("endDate").type(STRING).description("독서 종료일"),
                                        fieldWithPath("isUserNovelInterest").type(BOOLEAN)
                                                .description("현재 사용자의 관심 등록 여부"))
                                .build())));
    }

    private NovelGetResponseBasic novelBasicResponse() {
        return new NovelGetResponseBasic(
                1210L,
                "작품명",
                "https://image.websoso.kr/novel/2706.jpg",
                "판타지",
                "https://image.websoso.kr/genre/fantasy.png",
                true,
                "작가명",
                12,
                4.3f,
                8,
                5,
                3.0f,
                "WATCHING",
                "2026-09-01",
                "2026-09-10",
                true
        );
    }
}
