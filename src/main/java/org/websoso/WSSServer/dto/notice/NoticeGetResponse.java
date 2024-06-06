package org.websoso.WSSServer.dto.notice;

public record NoticeGetResponse(
        String noticeTitle,
        String noticeContent,
        String createdDate
) {
}
