package org.websoso.WSSServer.library.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.websoso.WSSServer.domain.common.ReadStatus.QUIT;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHED;
import static org.websoso.WSSServer.domain.common.ReadStatus.WATCHING;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.websoso.WSSServer.domain.common.ReadStatus;

/**
 * 사용자 서재 도메인의 상태별 독서 날짜 정책을 검증한다.
 */
class UserNovelTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 2, 1);

    // 읽는 중은 시작일, 완독과 하차는 마지막 날짜를 반환하는지 검증한다.
    @ParameterizedTest
    @MethodSource("readDateCases")
    @DisplayName("독서 상태에 맞는 날짜순 정렬 날짜를 반환한다")
    void getReadDateByStatus(ReadStatus status, LocalDate expected) {
        UserNovel userNovel = UserNovel.create(status, 0.0f, START_DATE, END_DATE, null, null);

        assertThat(userNovel.getReadDate()).isEqualTo(expected);
    }

    // 상태별 기대 독서 날짜 테스트 데이터를 제공한다.
    private static Stream<Arguments> readDateCases() {
        return Stream.of(
                Arguments.of(WATCHING, START_DATE),
                Arguments.of(WATCHED, END_DATE),
                Arguments.of(QUIT, END_DATE)
        );
    }
}
