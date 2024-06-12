package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.websoso.WSSServer.domain.common.BaseEntity;

@Entity
@Getter
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long commentId;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isHidden;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String commentContent;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    public static Comment create(Long userId, Feed feed, String commentContent) {
        return new Comment(commentContent, userId, feed);
    }

    private Comment(String commentContent, Long userId, Feed feed) {
        this.commentContent = commentContent;
        this.userId = userId;
        this.feed = feed;
    }

}
