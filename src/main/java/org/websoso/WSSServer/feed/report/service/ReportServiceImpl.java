package org.websoso.WSSServer.feed.report.service;

import static org.websoso.WSSServer.feed.report.exception.CustomReportError.ALREADY_REPORTED_COMMENT;
import static org.websoso.WSSServer.feed.report.exception.CustomReportError.ALREADY_REPORTED_FEED;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.domain.common.ReportedType;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.report.domain.ReportedComment;
import org.websoso.WSSServer.feed.report.domain.ReportedFeed;
import org.websoso.WSSServer.feed.report.exception.DuplicateReportException;
import org.websoso.WSSServer.feed.report.repository.ReportConstraintViolationDetector;
import org.websoso.WSSServer.feed.report.repository.ReportedCommentRepository;
import org.websoso.WSSServer.feed.report.repository.ReportedFeedRepository;
import org.websoso.WSSServer.user.domain.User;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl {

    private final ReportedCommentRepository reportedCommentRepository;
    private final ReportedFeedRepository reportedFeedRepository;
    private final ReportConstraintViolationDetector constraintViolationDetector;

    /**
     * 피드 신고 내역을 저장한다.
     * 중복 신고 제약조건 위반을 내부 중복 신고 예외로 변환할 수 있도록 즉시 flush한다.
     *
     * @param feed         신고 대상 피드
     * @param user         신고한 사용자
     * @param reportedType 신고 유형
     */
    @Transactional
    public void saveReportedFeed(Feed feed, User user, ReportedType reportedType) {
        try {
            reportedFeedRepository.saveAndFlush(ReportedFeed.create(feed, user, reportedType));
        } catch (DataIntegrityViolationException exception) {
            if (constraintViolationDetector.isDuplicateFeedReport(exception)) {
                throw new DuplicateReportException(ALREADY_REPORTED_FEED,
                        "feed has already been reported by the user");
            }
            throw exception;
        }
    }

    /**
     * 댓글 신고 내역을 저장한다.
     * 중복 신고 제약조건 위반을 내부 중복 신고 예외로 변환할 수 있도록 즉시 flush한다.
     *
     * @param comment      신고 대상 댓글
     * @param user         신고한 사용자
     * @param reportedType 신고 유형
     */
    @Transactional
    public void saveReportedComment(Comment comment, User user, ReportedType reportedType) {
        try {
            reportedCommentRepository.saveAndFlush(ReportedComment.create(comment, user, reportedType));
        } catch (DataIntegrityViolationException exception) {
            if (constraintViolationDetector.isDuplicateCommentReport(exception)) {
                throw new DuplicateReportException(ALREADY_REPORTED_COMMENT,
                        "comment has already been reported by the user");
            }
            throw exception;
        }
    }

    /**
     * 피드의 신고 유형별 누적 신고 수를 조회한다.
     *
     * @param feed         신고 대상 피드
     * @param reportedType 신고 유형
     * @return 누적 신고 수
     */
    @Transactional(readOnly = true)
    public int countByFeedAndReportedType(Feed feed, ReportedType reportedType) {
        return reportedFeedRepository.countByFeedAndReportedType(feed, reportedType);
    }

    /**
     * 댓글의 신고 유형별 누적 신고 수를 조회한다.
     *
     * @param comment      신고 대상 댓글
     * @param reportedType 신고 유형
     * @return 누적 신고 수
     */
    @Transactional(readOnly = true)
    public int countByCommentAndReportedType(Comment comment, ReportedType reportedType) {
        return reportedCommentRepository.countByCommentAndReportedType(comment, reportedType);
    }
}
