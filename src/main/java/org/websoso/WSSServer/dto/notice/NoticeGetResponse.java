package org.websoso.WSSServer.dto.notice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.websoso.WSSServer.domain.Notice;

public record NoticeGetResponse(
        String noticeTitle,
        String noticeContent,
        String noticeCategoryImage,
        String createdDate
) {

    public static NoticeGetResponse from(Notice notice) {
        return new NoticeGetResponse(
                notice.getNoticeTitle(),
                notice.getNoticeContent(),
                notice.getNoticeCategory().getNoticeCategoryImage(),
                formatDateString(notice.getCreatedDate())
        );
    }

    private static String formatDateString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }
}
