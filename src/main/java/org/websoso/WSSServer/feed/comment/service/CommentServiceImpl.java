package org.websoso.WSSServer.feed.comment.service;

import static org.websoso.WSSServer.feed.comment.exception.CustomCommentError.COMMENT_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.report.repository.ReportedCommentRepository;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.feed.comment.controller.dto.CommentCreateRequest;
import org.websoso.WSSServer.feed.comment.exception.CustomCommentException;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.feed.comment.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl {

    private final CommentRepository commentRepository;
    private final ReportedCommentRepository reportedCommentRepository;

    @Transactional
    public void createComment(User user, Feed feed, CommentCreateRequest request) {
        commentRepository.save(Comment.create(feed, user.getUserId(), request.commentContent()));
    }

    @Transactional(readOnly = true)
    public Comment getCommentOrException(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new CustomCommentException(COMMENT_NOT_FOUND, "comment with the given id was not found"));
    }

    /**
     * 댓글을 삭제한다.
     * 신고된 댓글이 Comment를 참조하므로, FK 제약을 피하기 위해 신고 내역을 먼저 삭제한다.
     *
     * @param comment 삭제할 댓글
     */
    @Transactional
    public void deleteComment(Comment comment) {
        reportedCommentRepository.deleteByComment(comment);
        commentRepository.delete(comment);
    }

    /**
     * 피드 삭제 시 해당 피드에 달린 댓글과 댓글 신고 내역을 함께 삭제한다.
     * 신고 내역이 Comment를 참조하므로 신고 내역을 먼저 삭제한 뒤 댓글을 삭제한다.
     *
     * @param feedId 삭제 대상 피드 ID
     */
    @Transactional
    public void deleteByFeedId(Long feedId) {
        reportedCommentRepository.deleteByFeedId(feedId);
        commentRepository.deleteByFeedId(feedId);
    }

    /**
     * 댓글 작성자 정보를 (알 수 없음)으로 변경한다.
     * 회원 탈퇴 시에 사용한다.
     *
     * @param userId 변경 대상 사용자 ID
     */
    @Transactional
    public void updateWriterToUnknown(Long userId) {
        commentRepository.updateUserToUnknown(userId);
    }
}
