package org.websoso.WSSServer.util;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatUtil {

    private static final long SECONDS_PER_MINUTE = 60;
    private static final long MINUTES_PER_HOUR = 60;
    private static final long HOURS_PER_DAY = 24;
    private static final long DAYS_PER_WEEK = 7;

    private static final DateTimeFormatter SAME_YEAR_FMT = DateTimeFormatter.ofPattern("M월 d일");
    private static final DateTimeFormatter DIFF_YEAR_FMT = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    public static String formatRelativeTime(LocalDateTime createdDate) {
        return formatRelativeTime(createdDate, Clock.systemDefaultZone());
    }

    public static String formatRelativeTime(LocalDateTime createdDate, Clock clock) {
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
}
