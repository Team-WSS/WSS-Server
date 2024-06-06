package org.websoso.WSSServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notice;
import org.websoso.WSSServer.dto.notice.NoticePostRequest;
import org.websoso.WSSServer.repository.NoticeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public void createNotice(NoticePostRequest noticePostRequest) {
        noticeRepository.save(Notice.builder()
                .noticeTitle(noticePostRequest.noticeTitle())
                .noticeContent(noticePostRequest.noticeContent())
                .userId(noticePostRequest.userId())
                .build());
    }
}
