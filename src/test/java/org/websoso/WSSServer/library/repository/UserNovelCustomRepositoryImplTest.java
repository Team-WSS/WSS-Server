package org.websoso.WSSServer.library.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.library.repository.cursor.UserNovelCursor;

/**
 * 사용자 서재의 날짜순 커서 조건을 검증한다.
 */
class UserNovelCustomRepositoryImplTest {

    private static final LocalDateTime CREATED_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);
    private final UserNovelCustomRepositoryImpl repository = new UserNovelCustomRepositoryImpl(null);

    // 날짜가 없는 커서 이후 항목을 등록일과 식별자 내림차순으로 조회하는지 검증한다.
    @Test
    @DisplayName("날짜가 없는 항목은 등록 최신순 커서를 적용한다")
    void createReadDateCursorConditionWithoutReadDate() {
        UserNovelCursor cursor = createCursor(null);

        BooleanExpression condition = ReflectionTestUtils.invokeMethod(
                repository, "readDateCursorCondition", cursor);

        assertThat(condition).isNotNull();
        assertThat(condition.toString())
                .contains("startDate", "endDate", "createdDate", "userNovelId");
    }

    // 날짜가 있는 커서 이후 항목을 독서 날짜와 식별자 내림차순으로 조회하는지 검증한다.
    @Test
    @DisplayName("날짜가 있는 항목은 상태별 독서 날짜 커서를 적용한다")
    void createReadDateCursorConditionWithReadDate() {
        UserNovelCursor cursor = createCursor(LocalDate.of(2026, 1, 1));

        BooleanExpression condition = ReflectionTestUtils.invokeMethod(
                repository, "readDateCursorCondition", cursor);

        assertThat(condition).isNotNull();
        assertThat(condition.toString())
                .contains("status", "WATCHING", "startDate", "endDate", "userNovelId")
                .doesNotContain("createdDate");
    }

    // 날짜순 커서 조건 테스트에 필요한 커서를 생성한다.
    private UserNovelCursor createCursor(LocalDate readDate) {
        return new UserNovelCursor(0.0f, CREATED_DATE, 1L, false, readDate, "제목");
    }
}
