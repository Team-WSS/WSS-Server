package org.websoso.WSSServer.dto.notice;

import java.util.List;
import java.util.stream.Collectors;
import org.websoso.WSSServer.domain.Notice;

public record NoticesGetResponse(
        List<NoticeGetResponse> notices
) {

    public static NoticesGetResponse of(List<Notice> notices) {
        List<NoticeGetResponse> noticeList = notices.stream()
                .map(NoticeGetResponse::from)
                .collect(Collectors.toList());
        return new NoticesGetResponse(noticeList);
    }
}
