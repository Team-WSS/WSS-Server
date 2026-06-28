package org.websoso.WSSServer.feed.comment.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.feed.comment.exception.CustomCommentError.COMMENT_NOT_BELONG_TO_FEED;
import static org.websoso.WSSServer.exception.error.CustomUserError.INVALID_AUTHORIZED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.common.Action;
import org.websoso.WSSServer.feed.comment.exception.CustomCommentException;
import org.websoso.WSSServer.exception.exception.CustomUserException;
import org.websoso.WSSServer.feed.feed.domain.Feed;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

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

    @Column(length = 500, nullable = false)
    @org.hibernate.annotations.Comment("댓글 내용")
    private String commentContent;

    @Column(nullable = false)
    @org.hibernate.annotations.Comment("신고에 의한 숨김 처리 여부")
    private boolean isHidden;

    @Column(nullable = false)
    @org.hibernate.annotations.Comment("신고에 의한 스포일러 처리 여부")
    private boolean isSpoiler;

    @Column(nullable = false)
    @org.hibernate.annotations.Comment("댓글 최초 작성 시각")
    private LocalDateTime createdDate;

    @Column(nullable = false)
    @org.hibernate.annotations.Comment("댓글 마지막 수정 시각")
    private LocalDateTime modifiedDate;

    public static Comment create(Long userId, Feed feed, String commentContent) {
        return new Comment(commentContent, userId, feed);
    }

    private Comment(String commentContent, Long userId, Feed feed) {
        this.commentContent = commentContent;
        this.userId = userId;
        this.feed = feed;
        this.isHidden = false;
        this.isSpoiler = false;
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = this.createdDate;
    }

    /**
     * 댓글 작성자인지 검증합니다.
     *
     * @throws CustomUserException 작성자가 아닌 경우
     */
    public void validateOwner(Long userId, Action action) {
        if (!Objects.equals(this.userId, userId)) {
            throw new CustomUserException(INVALID_AUTHORIZED,
                    "only the author can " + action.getLabel() + " the comment");
        }
    }

    /**
     * 댓글 내용을 수정합니다.
     * 수정 시각도 함께 갱신합니다.
     */
    public void updateContent(String commentContent) {
        this.commentContent = commentContent;
        this.modifiedDate = LocalDateTime.now();
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
     * 신고에 의해 댓글을 숨김 처리합니다.
     */
    public void hideComment() {
        this.isHidden = true;
    }

    /**
     * 신고에 의해 스포일러 처리합니다.
     */
    public void spoiler() {
        this.isSpoiler = true;
    }

}
