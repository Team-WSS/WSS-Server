package org.websoso.WSSServer.util;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeFormatUtil {

    private static final long SECONDS_PER_MINUTE = 60;
    private static final long MINUTES_PER_HOUR = 60;
    private static final long HOURS_PER_DAY = 24;
    private static final long DAYS_PER_WEEK = 7;

    private static final DateTimeFormatter SAME_YEAR_FMT = DateTimeFormatter.ofPattern("M월 d일");
    private static final DateTimeFormatter DIFF_YEAR_FMT = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
    private static final DateTimeFormatter NOTIFICATION_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * 피드 날짜 포맷
     * 60초 미만: 방금 전
     * 1시간 미만: N분 전
     * 24시간 미만: N시간 전
     * 7일 미만: N일 전
     * 동일 년도: M월 d일
     * 과거 년도: yyyy년 M월 d일
     */
    public static String formatFeedDate(LocalDateTime createdDate) {
        return formatFeedDate(createdDate, Clock.systemDefaultZone());
    }

    public static String formatFeedDate(LocalDateTime createdDate, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        Duration duration = Duration.between(createdDate, now);

        long seconds = duration.getSeconds();
        if (seconds < SECONDS_PER_MINUTE) {
            return "방금 전";
        }

        long minutes = duration.toMinutes();
        if (minutes < MINUTES_PER_HOUR) {
            return minutes + "분 전";
        }

        long hours = duration.toHours();
        if (hours < HOURS_PER_DAY) {
            return hours + "시간 전";
        }

        long days = duration.toDays();
        if (days < DAYS_PER_WEEK) {
            return days + "일 전";
        }

        if (createdDate.getYear() == now.getYear()) {
            return createdDate.format(SAME_YEAR_FMT);
        }

        return createdDate.format(DIFF_YEAR_FMT);
    }

    /**
     * 알림 목록용 날짜 포맷
     * 60초 미만: 방금 전
     * 1시간 미만: N분 전
     * 24시간 미만: N시간 전
     * 24시간 이상: yyyy.MM.dd 형식
     */
    public static String formatNotificationDate(LocalDateTime createdDate) {
        return formatNotificationDate(createdDate, Clock.systemDefaultZone());
    }

    public static String formatNotificationDate(LocalDateTime createdDate, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        Duration duration = Duration.between(createdDate, now);

        long seconds = duration.getSeconds();
        if (seconds < SECONDS_PER_MINUTE) {
            return "방금 전";
        }

        long minutes = duration.toMinutes();
        if (minutes < MINUTES_PER_HOUR) {
            return minutes + "분 전";
        }

        long hours = duration.toHours();
        if (hours < HOURS_PER_DAY) {
            return hours + "시간 전";
        }

        return createdDate.format(NOTIFICATION_FMT);
    }

}
