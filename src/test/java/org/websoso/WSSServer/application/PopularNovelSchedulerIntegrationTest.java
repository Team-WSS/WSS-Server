package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.config.JpaAuditingConfig;
import org.websoso.WSSServer.config.QueryDslConfig;
import org.websoso.WSSServer.domain.common.ReadStatus;
import org.websoso.WSSServer.library.domain.UserNovel;
import org.websoso.WSSServer.library.repository.UserNovelRepository;
import org.websoso.WSSServer.library.service.LibraryService;
import org.websoso.WSSServer.novel.domain.Novel;
import org.websoso.WSSServer.novel.domain.PopularNovel;
import org.websoso.WSSServer.novel.repository.PopularNovelRepository;
import org.websoso.WSSServer.novel.service.PopularNovelService;
import org.websoso.WSSServer.user.domain.User;

@DataJpaTest(showSql = false, properties = {
        "spring.config.location=classpath:/application-popular-novel-test.properties"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = PopularNovelSchedulerIntegrationTest.JpaTestConfig.class)
class PopularNovelSchedulerIntegrationTest {

    @Autowired
    private PopularNovelScheduler scheduler;

    @Autowired
    private PopularNovelRepository popularNovelRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("서재 데이터가 없으면 기존 인기작의 PK와 작품 ID를 그대로 유지한다")
    @Test
    void preservesPreviousPopularNovelsWhenLibraryIsEmpty() {
        List<PopularNovel> previous = savePopularNovels(101L, 102L);

        scheduler.updatePopularNovels();

        List<PopularNovel> actual = reloadPopularNovels();
        assertThat(actual).extracting(PopularNovel::getPopularNovelId)
                .containsExactlyInAnyOrderElementsOf(previous.stream()
                        .map(PopularNovel::getPopularNovelId).toList());
        assertThat(actual).extracting(PopularNovel::getNovelId)
                .containsExactlyInAnyOrder(101L, 102L);
    }

    @DisplayName("기존 인기작과 후보가 모두 없으면 빈 테이블을 유지한다")
    @Test
    void keepsTableEmptyWhenNoPreviousPopularNovelsOrCandidates() {
        scheduler.updatePopularNovels();

        assertThat(reloadPopularNovels()).isEmpty();
    }

    @DisplayName("7일 경계 이전 데이터와 비대상 상태만 있으면 기존 인기작을 유지한다")
    @Test
    void preservesPreviousPopularNovelsWhenNoEligibleLibraryEntries() {
        List<PopularNovel> previous = savePopularNovels(101L);
        User user = saveUser("reader");
        LocalDateTime cutoff = LocalDate.now().minusDays(7).atStartOfDay();
        saveLibrary(user, saveNovel("old watching"), WATCHING, false, cutoff.minusSeconds(1));
        saveLibrary(user, saveNovel("boundary watched"), WATCHED, false, cutoff);
        saveLibrary(user, saveNovel("old interest"), null, true, cutoff.minusDays(1));
        saveLibrary(user, saveNovel("quit"), QUIT, false, recentDate());
        saveLibrary(user, saveNovel("no status"), null, false, recentDate());
        flushAndClear();

        scheduler.updatePopularNovels();

        List<PopularNovel> actual = reloadPopularNovels();
        assertThat(actual).extracting(PopularNovel::getPopularNovelId)
                .containsExactly(previous.get(0).getPopularNovelId());
        assertThat(actual).extracting(PopularNovel::getNovelId).containsExactly(101L);
    }

    @DisplayName("최근 읽는 중·읽음·관심 작품으로 교체하고 기존과 겹치는 작품도 한 번만 남긴다")
    @Test
    void replacesPreviousPopularNovelsWithEligibleCandidates() {
        User user = saveUser("reader");
        Novel watching = saveNovel("watching");
        Novel watched = saveNovel("watched");
        Novel interest = saveNovel("interest");
        Novel quitInterest = saveNovel("quit interest");
        saveLibrary(user, watching, WATCHING, true, recentDate());
        saveLibrary(user, watched, WATCHED, false, recentDate());
        saveLibrary(user, interest, null, true, recentDate());
        saveLibrary(user, quitInterest, QUIT, true, recentDate());
        saveLibrary(user, saveNovel("quit"), QUIT, false, recentDate());
        saveLibrary(user, saveNovel("no status"), null, false, recentDate());
        saveLibrary(user, saveNovel("old interest"), null, true, recentDate().minusDays(7));
        List<PopularNovel> previous = savePopularNovels(watching.getNovelId(), 999L);

        scheduler.updatePopularNovels();

        List<PopularNovel> actual = reloadPopularNovels();
        assertThat(actual).extracting(PopularNovel::getNovelId).containsExactlyInAnyOrder(
                watching.getNovelId(), watched.getNovelId(), interest.getNovelId(), quitInterest.getNovelId());
        assertThat(actual).extracting(PopularNovel::getPopularNovelId)
                .doesNotContainAnyElementsOf(previous.stream().map(PopularNovel::getPopularNovelId).toList());

        scheduler.updatePopularNovels();

        assertThat(reloadPopularNovels()).extracting(PopularNovel::getNovelId)
                .containsExactlyInAnyOrder(watching.getNovelId(), watched.getNovelId(),
                        interest.getNovelId(), quitInterest.getNovelId());
    }

    @DisplayName("기존 인기작이 없어도 후보가 한 건이면 최초 인기작으로 저장한다")
    @Test
    void createsInitialPopularNovelFromSingleCandidate() {
        User user = saveUser("reader");
        Novel novel = saveNovel("interest");
        saveLibrary(user, novel, null, true, recentDate());
        flushAndClear();

        scheduler.updatePopularNovels();

        assertThat(reloadPopularNovels()).extracting(PopularNovel::getNovelId)
                .containsExactly(novel.getNovelId());
    }

    @DisplayName("갱신 성공 후 다음 실행에서 후보가 사라지면 마지막으로 저장된 인기작을 유지한다")
    @Test
    void preservesLastSuccessfulRankingWhenCandidatesDisappear() {
        User user = saveUser("reader");
        Novel novel = saveNovel("new popular novel");
        saveLibrary(user, novel, WATCHING, false, recentDate());
        savePopularNovels(999L);
        scheduler.updatePopularNovels();
        List<PopularNovel> lastSuccessfulRanking = reloadPopularNovels();
        assertThat(lastSuccessfulRanking).extracting(PopularNovel::getNovelId)
                .containsExactly(novel.getNovelId());

        entityManager.createQuery("delete from UserNovel").executeUpdate();
        flushAndClear();

        scheduler.updatePopularNovels();

        List<PopularNovel> actual = reloadPopularNovels();
        assertThat(actual).extracting(PopularNovel::getPopularNovelId)
                .containsExactly(lastSuccessfulRanking.get(0).getPopularNovelId());
        assertThat(actual).extracting(PopularNovel::getNovelId).containsExactly(novel.getNovelId());
    }

    @DisplayName("후보가 30개를 넘으면 서재 등록 수가 높은 상위 30개 작품만 저장한다")
    @Test
    void savesOnlyTopThirtyCandidatesByLibraryCount() {
        User first = saveUser("first");
        User second = saveUser("second");
        Novel leastPopular = saveNovel("least popular");
        saveLibrary(first, leastPopular, WATCHING, false, recentDate());
        List<Long> expectedNovelIds = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Novel novel = saveNovel("popular " + i);
            expectedNovelIds.add(novel.getNovelId());
            saveLibrary(first, novel, WATCHING, false, recentDate());
            saveLibrary(second, novel, WATCHED, false, recentDate());
        }
        savePopularNovels(999L);

        scheduler.updatePopularNovels();

        assertThat(reloadPopularNovels()).extracting(PopularNovel::getNovelId)
                .containsExactlyInAnyOrderElementsOf(expectedNovelIds);
    }

    private List<PopularNovel> savePopularNovels(Long... novelIds) {
        List<PopularNovel> saved = popularNovelRepository.saveAll(
                List.of(novelIds).stream().map(PopularNovel::from).toList());
        flushAndClear();
        return saved;
    }

    private User saveUser(String nickname) {
        User user = User.createBySocial("social-" + nickname, nickname, nickname + "@example.com");
        entityManager.persist(user);
        return user;
    }

    private Novel saveNovel(String title) {
        Novel novel = BeanUtils.instantiateClass(Novel.class);
        ReflectionTestUtils.setField(novel, "title", title);
        ReflectionTestUtils.setField(novel, "author", "author");
        ReflectionTestUtils.setField(novel, "novelImage", "https://example.com/novel.png");
        ReflectionTestUtils.setField(novel, "novelDescription", "description");
        ReflectionTestUtils.setField(novel, "isCompleted", false);
        entityManager.persist(novel);
        return novel;
    }

    private void saveLibrary(User user, Novel novel, ReadStatus status, boolean interested,
                             LocalDateTime createdDate) {
        UserNovel library = UserNovel.create(status, UserNovel.DEFAULT_RATING, null, null, user, novel);
        if (interested) {
            library.markAsInterested();
        }
        entityManager.persist(library);
        ReflectionTestUtils.setField(library, "createdDate", createdDate);
    }

    private LocalDateTime recentDate() {
        return LocalDate.now().minusDays(1).atTime(12, 0);
    }

    private List<PopularNovel> reloadPopularNovels() {
        flushAndClear();
        return popularNovelRepository.findAll();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // 운영 진입점의 Redis와 자동 스케줄 실행 없이 실제 JPA/QueryDSL 경로만 구성한다.
    @TestComponent
    @Configuration(proxyBeanMethods = false)
    @EntityScan("org.websoso.WSSServer")
    @EnableJpaRepositories(basePackageClasses = {PopularNovelRepository.class, UserNovelRepository.class})
    @Import({PopularNovelScheduler.class, PopularNovelService.class, LibraryService.class,
            JpaAuditingConfig.class, QueryDslConfig.class})
    static class JpaTestConfig {
    }
}
