package org.websoso.WSSServer.feed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.websoso.WSSServer.domain.common.FeedGetOption;
import org.websoso.WSSServer.oauth2.repository.RefreshTokenRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class FeedApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    EntityManager em;


    private static final Long USER_ID = 10034L;
    private static final Long NOVEL_ID = 3147L;
    private static final String TEST_GENRE_NAME = "romance";

    private Long genreId = 1L;
    private final List<Long> insertedFeedIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            // 피드1: 소설 연결 피드
            em.createNativeQuery("""
                    INSERT INTO feed (feed_content, novel_id, is_spoiler, is_public, is_hidden, user_id, created_date, modified_date)
                    VALUES ('테스트_소설연결피드', ?, FALSE, TRUE, FALSE, ?, NOW(), NOW())
                    """)
                    .setParameter(1, NOVEL_ID)
                    .setParameter(2, USER_ID)
                    .executeUpdate();
            insertedFeedIds.add(((Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue());

            // 피드2: etc 피드 (novelId = null)
            em.createNativeQuery("""
                    INSERT INTO feed (feed_content, novel_id, is_spoiler, is_public, is_hidden, user_id, created_date, modified_date)
                    VALUES ('테스트_소설없는피드1', NULL, FALSE, TRUE, FALSE, ?, NOW(), NOW())
                    """)
                    .setParameter(1, USER_ID)
                    .executeUpdate();
            insertedFeedIds.add(((Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue());

            // 피드3: etc 피드 (novelId = null)
            em.createNativeQuery("""
                    INSERT INTO feed (feed_content, novel_id, is_spoiler, is_public, is_hidden, user_id, created_date, modified_date)
                    VALUES ('테스트_소설없는피드2', NULL, FALSE, TRUE, FALSE, ?, NOW(), NOW())
                    """)
                    .setParameter(1, USER_ID)
                    .executeUpdate();
            insertedFeedIds.add(((Number) em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue());

            return null;
        });
    }

    private String getAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/users/login")
                                .contentType(APPLICATION_JSON)
                                .content(String.valueOf(USER_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("Authorization").asText();
    }

    // ============================================================
    // GET /feeds - category=all
    // ============================================================

    @Test
    void category_all_요청시_200_응답과_피드_목록이_반환된다() throws Exception {
        String token = getAccessToken();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/feeds")
                                .header("Authorization", "Bearer " + token)
                                .param("lastFeedId", "0")
                                .param("size", "100"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        System.out.println(body);
        int feedCount = objectMapper.readTree(body).get("feeds").size();
        assertThat(feedCount).isGreaterThanOrEqualTo(3);
    }




    // ============================================================
    // GET /feeds - 비로그인(anonymous)
    // ============================================================

    @Test
    void 비로그인_상태로_category_all_요청시_200_응답이_반환된다() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/feeds")
                                .param("lastFeedId", "0")
                                .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(objectMapper.readTree(body).get("feeds")).isNotNull();
    }



    private MockMultipartFile feedPart(String json) {
        return new MockMultipartFile("feed", null, "application/json",
                json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static final String CREATE_FEED_CONTENT = "API생성_테스트피드내용";
    private static final String CREATE_FEED_CONTENT_COMPAT = "API생성_하위호환테스트피드";

    @Test
    void 피드_생성시_relevantCategories_없이_201이_반환된다() throws Exception {
        String token = getAccessToken();
        String feedJson = String.format(
                "{\"feedContent\":\"%s\",\"novelId\":null,\"isSpoiler\":false,\"isPublic\":true}",
                CREATE_FEED_CONTENT);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/feeds")
                                .file(feedPart(feedJson))
                                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        var responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(responseNode.has("imagesCount")).isTrue();
    }

    @Test
    void 피드_생성시_relevantCategories_포함해도_201이_반환된다() throws Exception {
        String token = getAccessToken();
        // 기존 클라이언트가 relevantCategories를 포함해서 요청해도 에러가 나지 않는지 검증 (하위호환)
        String feedJsonWithOldField = String.format(
                "{\"feedContent\":\"%s\",\"novelId\":null,\"isSpoiler\":false,\"isPublic\":true,\"relevantCategories\":[\"로맨스\",\"판타지\"]}",
                CREATE_FEED_CONTENT_COMPAT);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/feeds")
                                .file(feedPart(feedJsonWithOldField))
                                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        var responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(responseNode.has("imagesCount")).isTrue();
    }

    // ============================================================
    // GET /feeds/{feedId} - FeedGetResponse에 relevantCategories 없음
    // ============================================================

    @Test
    void 단건_피드_조회시_응답에_relevantCategories_필드가_없다() throws Exception {
        Long feedId = insertedFeedIds.get(0);
        String token = getAccessToken();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/feeds/{feedId}", feedId)
                                .header("Authorization", "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(responseNode.has("relevantCategories")).isFalse();
    }

    @Test
    void 유저_피드_목록_조회시_응답에_relevantCategories_필드가_없다() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{userId}/feeds", USER_ID)
                                .param("lastFeedId", "0")
                                .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var feeds = objectMapper.readTree(result.getResponse().getContentAsString()).get("feeds");
        assertThat(feeds.size()).isGreaterThanOrEqualTo(1);
        feeds.forEach(feed -> assertThat(feed.has("relevantCategories")).isFalse());
    }

    @Test
    void 유저_피드_목록_조회시_genreNames에_etc_포함하면_novelId_null_피드만_반환된다() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{userId}/feeds", USER_ID)
                                .param("lastFeedId", "0")
                                .param("size", "10")
                                .param("genreNames", "etc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var feeds = objectMapper.readTree(result.getResponse().getContentAsString()).get("feeds");
        assertThat(feeds.size()).isGreaterThanOrEqualTo(1);
        feeds.forEach(feed -> assertThat(feed.get("novelId").isNull()).isTrue());
    }

    @Test
    void 피드_목록_조회시_선호_장르로만_검색하면_etc_피드가_포함되지_않는다() throws Exception {
        String token = getAccessToken();
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/feeds")
                                .header("Authorization", "Bearer " + token)
                                .param("lastFeedId", "0")
                                .param("size", "100")
                                .param("feedsOption", "RECOMMENDED"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var feeds = objectMapper.readTree(result.getResponse().getContentAsString()).get("feeds");

        feeds.forEach(feed -> {assertThat(feed.get("novelId").isNull()).isFalse();
        });
    }

    @Test
    void 유저_피드_목록_조회시_장르가_etc이면_작품_없는_피드만_조회되야_한다() throws Exception {
        String token = getAccessToken();
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{userId}/feeds", USER_ID)
                                .header("Authorization", "Bearer " + token)
                                .param("lastFeedId", "0")
                                .param("size", "10")
                                .param("genreNames", "etc"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var feeds = objectMapper.readTree(result.getResponse().getContentAsString()).get("feeds");
        assertThat(feeds.size()).isGreaterThanOrEqualTo(1);
        feeds.forEach(feed -> assertThat(feed.get("genre").isNull()).isTrue());
    }

    @Test
    void 유저_피드_목록_조회시_장르가_여러개일때_여러_장르의_작품이_조회되야_한다() throws Exception {
        String token = getAccessToken();
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{userId}/feeds", USER_ID)
                                .header("Authorization", "Bearer " + token)
                                .param("lastFeedId", "0")
                                .param("size", "10")
                                .param("genreNames", "etc")
                                .param("genreNames", "romance"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var feeds = objectMapper.readTree(result.getResponse().getContentAsString()).get("feeds");
        assertThat(feeds.size()).isGreaterThanOrEqualTo(2);
        feeds.forEach(feed -> {
            var genreNode = feed.get("genre");
            boolean isNullOrRomance = genreNode.isNull() || "romance".equals(genreNode.asText());

            assertThat(isNullOrRomance).isTrue();
        });
    }

    @Test
    void 유저_피드_목록_조회시_장르에_etc_없으면_작품이_조회되야_한다() throws Exception {
        String token = getAccessToken();
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/users/{userId}/feeds", USER_ID)
                                .header("Authorization", "Bearer " + token)
                                .param("lastFeedId", "0")
                                .param("size", "10")
                                .param("genreNames", "etc")
                                .param("genreNames", "romance"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        var feeds = objectMapper.readTree(result.getResponse().getContentAsString()).get("feeds");
        assertThat(feeds.size()).isGreaterThanOrEqualTo(2);
        feeds.forEach(feed -> {
            var genreNode = feed.get("genre");
            boolean isNullOrRomance = genreNode.isNull() || "romance".equals(genreNode.asText());

            assertThat(isNullOrRomance).isTrue();
        });
    }
}