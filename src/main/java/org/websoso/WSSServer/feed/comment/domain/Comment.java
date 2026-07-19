package org.websoso.WSSServer.feed.comment.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.feed.comment.exception.CustomCommentError.COMMENT_CONTENT_EMPTY;
import static org.websoso.WSSServer.feed.comment.exception.CustomCommentError.COMMENT_NOT_BELONG_TO_FEED;
import static org.websoso.WSSServer.feed.comment.exception.CustomCommentError.COMMENT_CONTENT_TOO_LONG;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.websoso.WSSServer.feed.comment.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.common.entity.BaseEntity;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    @org.hibernate.annotations.Comment("댓글 PK")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    @org.hibernate.annotations.Comment("댓글이 작성된 피드 PK")
    private Feed feed;

    @Column(nullable = false)
    @org.hibernate.annotations.Comment("댓글을 작성한 사용자 PK")
    private Long userId;

    @Column(name = "comment_content", length = 500, nullable = false)
    @org.hibernate.annotations.Comment("댓글 내용")
    private String content;

    @Column(nullable = false)
    @org.hibernate.annotations.Comment("신고에 의한 숨김 처리 여부")
    private boolean isHidden;

    @Column(nullable = false)
    @org.hibernate.annotations.Comment("신고에 의한 스포일러 처리 여부")
    private boolean isSpoiler;

    private static final int MAX_CONTENT_LENGTH = 500;

    public static Comment create(Feed feed, Long userId, String content) {
        return new Comment(feed, userId, content);
    }

    private Comment(Feed feed, Long userId, String content) {
        validateContent(content);

        this.feed = feed;
        this.userId = userId;
        this.content = content;

        this.isHidden = false;
        this.isSpoiler = false;
    }

    /**
     * 댓글 작성자인지 검증합니다.
     *
     * @throws CustomUserException 작성자가 아닌 경우
     */
    public void validateOwner(Long userId) {
        if (!Objects.equals(this.userId, userId)) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "only the author can modify the comment");
        }
    }

    /**
     * 댓글 작성자인지 확인합니다.
     */
    public boolean isMine(Long userId) {
        return Objects.equals(this.userId, userId);
    }

    /**
     * 댓글이 지정한 피드에 속하는지 검증합니다.
     *
     * @throws CustomCommentException 다른 피드의 댓글인 경우
     */
    public void validateBelongsTo(Feed feed) {
        if (this.feed != feed) {
            throw new CustomCommentException(COMMENT_NOT_BELONG_TO_FEED,
                    "the comment does not belong to the specified feed");
        }
    }

    /**
     * 댓글 내용을 수정합니다.
     */
    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
    }

    /**
     * 신고에 의해 댓글을 숨김 처리합니다.
     */
    public void markHidden() {
        this.isHidden = true;
    }

    /**
     * 신고에 의해 스포일러 처리합니다.
     */
    public void markSpoiler() {
        this.isSpoiler = true;
    }

    /**
     * 댓글 내용이 작성 가능한 형식인지 검증합니다.
     *
     * @throws CustomCommentException 검증에 실패한 경우
     */
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new CustomCommentException(COMMENT_CONTENT_EMPTY,
                    "comment content cannot be null or blank");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new CustomCommentException(COMMENT_CONTENT_TOO_LONG,
                    "comment content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
        }
    }

}
