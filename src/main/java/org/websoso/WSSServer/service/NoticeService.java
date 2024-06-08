package org.websoso.WSSServer.service;

import static org.websoso.WSSServer.domain.common.Role.ADMIN;
import static org.websoso.WSSServer.exception.notice.NoticeErrorCode.NOTICE_FORBIDDEN;
import static org.websoso.WSSServer.exception.notice.NoticeErrorCode.NOTICE_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.Notice;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.domain.common.Role;
import org.websoso.WSSServer.dto.notice.NoticeEditRequest;
import org.websoso.WSSServer.dto.notice.NoticePostRequest;
import org.websoso.WSSServer.dto.notice.NoticesGetResponse;
import org.websoso.WSSServer.exception.notice.exception.ForbiddenNoticeManipulationException;
import org.websoso.WSSServer.exception.notice.exception.NoticeNotFoundException;
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

    @Transactional
    public void editNotice(User user, Long noticeId, NoticeEditRequest noticeEditRequest) {
        validateAuthorization(user);
        Notice notice = getNoticeOrException(noticeId);
        notice.updateNotice(noticeEditRequest.noticeTitle(), noticeEditRequest.noticeContent(),
                noticeEditRequest.userId());
    }

    private static void validateAuthorization(User user) {
        if (user.getRole() != ADMIN_ROLE) {
            throw new ForbiddenNoticeManipulationException(NOTICE_FORBIDDEN,
                    "user who tried to create or modify or delete the notice is not ADMIN");
        }
    }

    public NoticesGetResponse getNotices(User user) {
        List<Notice> notices = noticeRepository.findByUserId(user.getUserId());
        return NoticesGetResponse.of(notices);
    }

    @Transactional
    public void deleteNotice(User user, Long noticeId) {
        validateAuthorization(user);
        Notice notice = getNoticeOrException(noticeId);
        noticeRepository.delete(notice);
    }

    private Notice getNoticeOrException(Long noticeId) {
        return noticeRepository.findById(noticeId).orElseThrow(() ->
                new NoticeNotFoundException(NOTICE_NOT_FOUND, "notice with given noticeId was not found"));
    }
}
