package org.websoso.WSSServer.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TimeFormatUtilTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    @Nested
    @DisplayName("피드 날짜 포맷팅 테스트")
    class FormatFeedDate {

        @DisplayName("60초 미만이면 '방금 전'을 반환한다")
        @Test
        void returnsJustNowForUnder60Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 30)).isEqualTo("방금 전");
        }

        @DisplayName("59초일 때 '방금 전'을 반환한다")
        @Test
        void returnsJustNowAt59Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 59)).isEqualTo("방금 전");
        }

        @DisplayName("60초이면 '1분 전'을 반환한다")
        @Test
        void returns1MinuteAt60Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 60)).isEqualTo("1분 전");
        }

        @DisplayName("59분이면 '59분 전'을 반환한다")
        @Test
        void returns59MinutesAt59Minutes() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 59 * 60)).isEqualTo("59분 전");
        }

        @DisplayName("1시간이면 '1시간 전'을 반환한다")
        @Test
        void returns1HourAt3600Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 3600)).isEqualTo("1시간 전");
        }

        @DisplayName("23시간이면 '23시간 전'을 반환한다")
        @Test
        void returns23HoursAt23Hours() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 23 * 3600)).isEqualTo("23시간 전");
        }

        @DisplayName("24시간이면 '1일 전'을 반환한다")
        @Test
        void returns1DayAt24Hours() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 24 * 3600)).isEqualTo("1일 전");
        }

        @DisplayName("6일이면 '6일 전'을 반환한다")
        @Test
        void returns6DaysAt6Days() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatFeed(clock, 6 * 24 * 3600)).isEqualTo("6일 전");
        }

        @DisplayName("같은 해이면 '월 일' 형식을 반환한다")
        @Test
        void returnsMonthDayForSameYear() {
            Clock clock = fixedClock("2024-06-15T12:00:00");
            LocalDateTime createdAt = LocalDateTime.parse("2024-01-01T00:00:00");
            assertThat(TimeFormatUtil.formatFeedDate(createdAt, clock)).isEqualTo("1월 1일");
        }

        @DisplayName("다른 해이면 '년 월 일' 형식을 반환한다")
        @Test
        void returnsYearMonthDayForDifferentYear() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            LocalDateTime createdAt = LocalDateTime.parse("2023-12-31T00:00:00");
            assertThat(TimeFormatUtil.formatFeedDate(createdAt, clock)).isEqualTo("2023년 12월 31일");
        }

        @DisplayName("미래 시각이면 '방금 전'을 반환한다")
        @Test
        void returnsJustNowForFutureTime() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            LocalDateTime future = LocalDateTime.now(clock).plusHours(1);
            assertThat(TimeFormatUtil.formatFeedDate(future, clock)).isEqualTo("방금 전");
        }
    }

    @Nested
    @DisplayName("알림 날짜 포맷팅 테스트")
    class FormatNotificationDate {

        @DisplayName("60초 미만이면 '방금 전'을 반환한다")
        @Test
        void returnsJustNowForUnder60Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatNotification(clock, 30)).isEqualTo("방금 전");
        }

        @DisplayName("59초일 때 '방금 전'을 반환한다")
        @Test
        void returnsJustNowAt59Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatNotification(clock, 59)).isEqualTo("방금 전");
        }

        @DisplayName("60초이면 '1분 전'을 반환한다")
        @Test
        void returns1MinuteAt60Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatNotification(clock, 60)).isEqualTo("1분 전");
        }

        @DisplayName("59분이면 '59분 전'을 반환한다")
        @Test
        void returns59MinutesAt59Minutes() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatNotification(clock, 59 * 60)).isEqualTo("59분 전");
        }

        @DisplayName("1시간이면 '1시간 전'을 반환한다")
        @Test
        void returns1HourAt3600Seconds() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatNotification(clock, 3600)).isEqualTo("1시간 전");
        }

        @DisplayName("23시간이면 '23시간 전'을 반환한다")
        @Test
        void returns23HoursAt23Hours() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatNotification(clock, 23 * 3600)).isEqualTo("23시간 전");
        }

        @DisplayName("24시간 이상이면 'yyyy.MM.dd' 형식을 반환한다")
        @Test
        void returnsDateFormatOver24Hours() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            assertThat(formatNotification(clock, 24 * 3600)).isEqualTo("2024.01.14");
        }

        @DisplayName("연도가 넘어가도 'yyyy.MM.dd' 형식을 반환한다")
        @Test
        void returnsDateFormatAcrossYears() {
            Clock clock = fixedClock("2024-01-01T00:00:00");
            LocalDateTime createdAt = LocalDateTime.parse("2023-12-31T00:00:00");
            assertThat(TimeFormatUtil.formatNotificationDate(createdAt, clock)).isEqualTo("2023.12.31");
        }

        @DisplayName("미래 시각이면 '방금 전'을 반환한다")
        @Test
        void returnsJustNowForFutureTime() {
            Clock clock = fixedClock("2024-01-15T12:00:00");
            LocalDateTime future = LocalDateTime.now(clock).plusHours(1);
            assertThat(TimeFormatUtil.formatNotificationDate(future, clock)).isEqualTo("방금 전");
        }
    }

    private Clock fixedClock(String isoDateTime) {
        return Clock.fixed(
                LocalDateTime.parse(isoDateTime)
                        .atZone(ZONE)
                        .toInstant(),
                ZONE
        );
    }

    private String formatFeed(Clock clock, long secondsBefore) {
        LocalDateTime createdAt = LocalDateTime.now(clock).minusSeconds(secondsBefore);
        return TimeFormatUtil.formatFeedDate(createdAt, clock);
    }

    private String formatNotification(Clock clock, long secondsBefore) {
        LocalDateTime createdAt = LocalDateTime.now(clock).minusSeconds(secondsBefore);
        return TimeFormatUtil.formatNotificationDate(createdAt, clock);
    }
}
