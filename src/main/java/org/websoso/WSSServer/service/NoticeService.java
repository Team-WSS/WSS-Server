package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.notice.NoticeErrorCode.NOTICE_FORBIDDEN;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notice;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.Role;
import org.websoso.WSSServer.dto.notice.NoticePostRequest;
import org.websoso.WSSServer.exception.notice.exception.ForbiddenNoticeCreationException;
import org.websoso.WSSServer.repository.NoticeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private static final Role ADMIN_ROLE = ADMIN;

    @Transactional
    public void createNotice(User user, NoticePostRequest noticePostRequest) {
        validateAuthorization(user);
        noticeRepository.save(Notice.builder()
                .noticeTitle(noticePostRequest.noticeTitle())
                .noticeContent(noticePostRequest.noticeContent())
                .userId(noticePostRequest.userId())
                .build());
    }

    private static void validateAuthorization(User user) {
        if (user.getRole() != ADMIN_ROLE) {
            throw new ForbiddenNoticeCreationException(NOTICE_FORBIDDEN, "user is not ADMIN");
        }
    }
}
