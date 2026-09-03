package org.websoso.WSSServer.feed.feed;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.websoso.WSSServer.feed.feed.application.PopularFeedApplication;
import org.websoso.WSSServer.feed.feed.controller.FeedController;

/**
 * 이슈 #620 동시 좋아요 재현 자산이 프로덕션 계약과 어긋나지 않는지 정적으로 검증한다.
 * <p>
 * 재현 스크립트는 실행에 서버/DB/k6가 필요해 자동 테스트로 돌릴 수 없다. 대신 스크립트가 참조하는
 * 엔드포인트 경로와 임계값이 실제 코드와 같은지, 자격 증명이 섞여 들어가지 않았는지를 여기서 막는다.
 */
class PopularFeedLikeRaceScriptTest {

    private static final Path SCRIPT_DIR = Path.of("scripts", "load", "popular-feed-like-race");
    private static final Path K6_SCRIPT = SCRIPT_DIR.resolve("like-race.js");
    private static final Path RUNNER = SCRIPT_DIR.resolve("run-race.sh");
    private static final Path LIB = SCRIPT_DIR.resolve("lib.sh");
    private static final Path SEED = SCRIPT_DIR.resolve("seed-fixture.sh");
    private static final Path SUMMARIZER = SCRIPT_DIR.resolve("summarize.py");
    private static final Path TIMELINE = SCRIPT_DIR.resolve("observe-timeline.sh");

    @Test
    @DisplayName("재현 자산 파일이 모두 존재한다")
    void reproductionAssetsExist() {
        assertThat(List.of(K6_SCRIPT, RUNNER, LIB, SEED, SUMMARIZER, TIMELINE))
                .allSatisfy(path -> assertThat(Files.isRegularFile(path))
                        .as("%s 가 있어야 한다", path)
                        .isTrue());
    }

    @Test
    @DisplayName("k6 시나리오가 호출하는 경로가 좋아요 API 매핑과 같다")
    void k6ScriptTargetsTheRealLikeEndpoint() throws Exception {
        String mapping = FeedController.class
                .getDeclaredMethod("likeFeed", org.websoso.WSSServer.user.domain.User.class, Long.class)
                .getAnnotation(PostMapping.class)
                .value()[0];

        // "/feeds/{feedId}/likes" -> k6 템플릿 리터럴 "/feeds/${FEED_ID}/likes"
        String expected = mapping.replace("{feedId}", "${FEED_ID}");

        assertThat(read(K6_SCRIPT))
                .as("k6 시나리오가 실제 매핑 경로 %s 를 호출해야 한다", mapping)
                .contains(expected);
    }

    @Test
    @DisplayName("기본 동시 사용자 수가 인기 피드 임계값보다 하나 많다")
    void defaultVusIsOneMoreThanPopularFeedThreshold() throws Exception {
        Field field = PopularFeedApplication.class.getDeclaredField("POPULAR_FEED_LIKE_THRESHOLD");
        field.setAccessible(true);
        int threshold = field.getInt(null);

        assertThat(defaultVus())
                .as("임계값(%d)을 넘어서는 마지막 한 명이 있어야 count == %d 판정 경쟁이 생긴다", threshold, threshold)
                .isEqualTo(threshold + 1);
    }

    @Test
    @DisplayName("반복 횟수와 동시 시작 방식을 조절할 수 있다")
    void runnerExposesRepeatAndSynchronizationKnobs() throws IOException {
        String runner = read(RUNNER);

        assertThat(runner).as("반복 횟수 옵션").contains("--rounds");
        assertThat(runner).as("동시 사용자 수 옵션").contains("--vus");
        assertThat(runner).as("동시 시작 리드타임 옵션").contains("--start-delay-ms");
        assertThat(read(K6_SCRIPT)).as("공통 시작 시각 동기화").contains("startAtMs");
    }

    @Test
    @DisplayName("라운드 초기화는 대상 피드에만 한정된다")
    void resetIsScopedToTheTargetFeed() throws IOException {
        String lib = read(LIB);

        Matcher deletes = Pattern.compile("DELETE\\s+(?:\\w+\\s+)?FROM\\s+[^;]+;").matcher(lib);
        int count = 0;
        while (deletes.find()) {
            count++;
            assertThat(deletes.group())
                    .as("초기화 DELETE는 feed_id로 범위를 좁혀야 한다")
                    .contains("feed_id");
        }
        assertThat(count).as("초기화 DELETE 문이 있어야 한다").isPositive();
    }

    @Test
    @DisplayName("운영·공유 환경을 대상으로 실행할 수 없도록 로컬 호스트만 허용한다")
    void runnerRefusesNonLocalTargets() throws IOException {
        String lib = read(LIB);

        assertThat(lib).contains("LOCAL_HOSTS_REGEX");
        assertThat(lib).contains("assert_local_target");
        assertThat(read(RUNNER)).as("실행 전 로컬 여부를 확인해야 한다").contains("assert_local_target");
        assertThat(read(SEED)).as("픽스처 생성 전에도 로컬 여부를 확인해야 한다").contains("assert_local_target");
        assertThat(read(TIMELINE)).as("타임라인 관찰도 로컬에서만 해야 한다").contains("assert_local_target");
    }

    @Test
    @DisplayName("SQL에 들어가는 입력은 조회·삭제보다 먼저 형식을 검증한다")
    void sqlInputsAreValidatedBeforeAnyQuery() throws IOException {
        String lib = read(LIB);

        assertThat(lib).as("양의 정수 검증").contains("require_positive_int");
        assertThat(lib).as("음이 아닌 정수 검증").contains("require_non_negative_int");
        assertThat(lib).as("SQL 문자열에 들어가는 태그 검증").contains("require_sql_safe_tag");
        assertThat(lib).as("FEED_ID 전용 검증").contains("require_feed_id");

        // 피드를 건드리는 함수는 각자 실행 직전에 FEED_ID를 다시 확인해야 한다.
        for (String function : List.of("feed_like_count", "popular_feed_count", "feed_exists",
                "reset_feed_state")) {
            assertThat(bodyOf(lib, function))
                    .as("%s 는 조회·삭제 전에 require_feed_id를 호출해야 한다", function)
                    .contains("require_feed_id");
        }

        String runner = read(RUNNER);
        assertThat(runner).as("실행기가 FEED_ID를 검증해야 한다").contains("require_feed_id");
        for (String name : List.of("ROUNDS", "VUS")) {
            assertThat(runner).as("%s 는 양의 정수여야 한다", name)
                    .contains("require_positive_int \"" + name + "\"");
        }
        for (String name : List.of("START_DELAY_MS", "SPIN_MS", "STAGGER_MS", "SETTLE_MS")) {
            assertThat(runner).as("%s 는 0 이상의 정수여야 한다", name)
                    .contains("require_non_negative_int \"" + name + "\"");
        }

        String seed = read(SEED);
        assertThat(seed).as("FIXTURE_TAG는 SQL 리터럴에 들어가므로 문자 집합을 검증해야 한다")
                .contains("require_sql_safe_tag \"FIXTURE_TAG\"");
        assertThat(seed).as("LIKER_COUNT는 양의 정수여야 한다")
                .contains("require_positive_int \"LIKER_COUNT\"");
    }

    @Test
    @DisplayName("타임라인 관찰이 기존 general log 행을 지우지 않는다")
    void timelineObserverPreservesExistingGeneralLog() throws IOException {
        String timeline = read(TIMELINE);

        assertThat(timeline)
                .as("mysql.general_log를 TRUNCATE하면 로컬에 쌓인 로그가 사라진다")
                .doesNotContainPattern("(?i)TRUNCATE\\s+TABLE\\s+mysql\\.general_log");
        assertThat(timeline).as("구간을 자를 marker가 있어야 한다").contains("MARKER");
        assertThat(timeline).as("marker 시각 이후만 읽어야 한다").contains("event_time >= @marker_at");
        assertThat(timeline).as("타임라인 관찰도 숫자 입력을 검증해야 한다").contains("require_feed_id");
    }

    @Test
    @DisplayName("집계기는 관측값만 분류하고 원인을 단정하지 않는다")
    void summarizerClassifiesObservationsOnly() throws IOException {
        String summarizer = read(SUMMARIZER);

        assertThat(summarizer)
                .as("popular_feed 행 상태와 요청 오류를 분리해야 한다")
                .contains("classify_popular_state")
                .contains("classify_request_state");
        assertThat(summarizer)
                .as("popular_feed 행 개수에서 바로 나오는 상태만 쓴다")
                .contains("not_registered")
                .contains("duplicate_rows");
        assertThat(summarizer)
                .as("5xx만으로 유니크 충돌이라고 단정하던 상태값은 남아 있으면 안 된다")
                .doesNotContain("duplicate_conflict");
        assertThat(summarizer)
                .as("요청 오류는 별도 상태로 기록해야 한다")
                .contains("like_count_mismatch")
                .contains("transport_error");

        assertThat(read(RUNNER))
                .as("CSV가 두 상태를 각각 컬럼으로 가져야 한다")
                .contains("popular_state,request_state");
    }

    @Test
    @DisplayName("재현 자산에 토큰이나 시크릿 리터럴이 들어 있지 않다")
    void reproductionAssetsCarryNoSecrets() {
        assertThat(List.of(K6_SCRIPT, RUNNER, LIB, SEED, SUMMARIZER, TIMELINE))
                .allSatisfy(path -> {
                    String content = read(path);
                    assertThat(content)
                            .as("%s 에 JWT 리터럴이 있으면 안 된다", path)
                            .doesNotContain("eyJhbGciOi");
                    assertThat(content)
                            .as("%s 는 시크릿을 환경 변수로만 받아야 한다", path)
                            .doesNotContainPattern("JWT_SECRET\\s*=\\s*[\"']?[A-Za-z0-9]");
                });
    }

    /** 셸 함수 하나의 본문을 잘라낸다. `name() {` 부터 열의 맨 앞 `}` 까지. */
    private String bodyOf(String script, String functionName) {
        Matcher matcher = Pattern
                .compile("^" + Pattern.quote(functionName) + "\\(\\)\\s*\\{$(.*?)^\\}$",
                        Pattern.DOTALL | Pattern.MULTILINE)
                .matcher(script);
        assertThat(matcher.find()).as("%s 함수를 찾지 못했다", functionName).isTrue();
        return matcher.group(1);
    }

    private int defaultVus() throws IOException {
        Matcher matcher = Pattern.compile("intEnv\\('VUS',\\s*(\\d+)\\)").matcher(read(K6_SCRIPT));
        assertThat(matcher.find()).as("like-race.js에 VUS 기본값이 있어야 한다").isTrue();
        return Integer.parseInt(matcher.group(1));
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(path + " 를 읽을 수 없다", e);
        }
    }
}
