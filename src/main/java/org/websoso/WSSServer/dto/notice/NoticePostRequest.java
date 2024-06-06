package org.websoso.WSSServer.dto.notice;

public record NoticePostRequest(
        String noticeTitle,
        String noticeContent,
        Long userId
) {
}
