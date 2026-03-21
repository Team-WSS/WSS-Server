package org.websoso.WSSServer.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class TimeFormatUtilTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    @Test
    void 방금_전() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 30)).isEqualTo("방금 전");
    }

    @Test
    void 방금_전_경계값_59초() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 59)).isEqualTo("방금 전");
    }

    @Test
    void 분_전() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 60)).isEqualTo("1분 전");
    }

    @Test
    void 분_전_경계값_59분() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 59 * 60)).isEqualTo("59분 전");
    }

    @Test
    void 시간_전() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 3600)).isEqualTo("1시간 전");
    }

    @Test
    void 시간_전_경계값_23시간() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 23 * 3600)).isEqualTo("23시간 전");
    }

    @Test
    void 일_전() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 24 * 3600)).isEqualTo("1일 전");
    }

    @Test
    void 일_전_경계값_6일() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        assertThat(format(clock, 6 * 24 * 3600)).isEqualTo("6일 전");
    }

    @Test
    void 같은_해_날짜_포맷() {
        Clock clock = fixedClock("2024-06-15T12:00:00");
        LocalDateTime createdAt = LocalDateTime.parse("2024-01-01T00:00:00");
        assertThat(TimeFormatUtil.formatRelativeTime(createdAt, clock)).isEqualTo("1월 1일");
    }

    @Test
    void 다른_해_날짜_포맷() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        LocalDateTime createdAt = LocalDateTime.parse("2023-12-31T00:00:00");
        assertThat(TimeFormatUtil.formatRelativeTime(createdAt, clock)).isEqualTo("2023년 12월 31일");
    }

    @Test
    void 미래_시각은_방금_전() {
        Clock clock = fixedClock("2024-01-15T12:00:00");
        LocalDateTime future = LocalDateTime.now(clock).plusHours(1);
        assertThat(TimeFormatUtil.formatRelativeTime(future, clock)).isEqualTo("방금 전");
    }

    private Clock fixedClock(String isoDateTime) {
        return Clock.fixed(
                LocalDateTime.parse(isoDateTime)
                        .atZone(ZONE)
                        .toInstant(),
                ZONE
        );
    }

    private String format(Clock clock, long secondsBefore) {
        LocalDateTime createdAt = LocalDateTime.now(clock).minusSeconds(secondsBefore);
        return TimeFormatUtil.formatRelativeTime(createdAt, clock);
    }

}
