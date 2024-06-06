package org.websoso.WSSServer.dto.notice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.domain.Notice;

public record NoticeGetResponse(
        String noticeTitle,
        String noticeContent,
        String createdDate
) {

    public static NoticeGetResponse from(Notice notice) {
        return new NoticeGetResponse(
                notice.getNoticeTitle(),
                notice.getNoticeContent(),
                formatDateString(notice.getCreatedDate())
        );
    }

    private static String formatDateString(String dateTime) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd a hh:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        LocalDateTime date = LocalDateTime.parse(dateTime, inputFormatter);
        return date.format(outputFormatter);
    }
}
