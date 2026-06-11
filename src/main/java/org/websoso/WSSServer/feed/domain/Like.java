package org.websoso.WSSServer.feed.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.websoso.common.entity.BaseEntity;

@Entity
@Getter
@Table(
        name = "`like`",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_like_user_id_feed_id",
                        columnNames = {"user_id", "feed_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long likeId;

    @Column(name = "user_id", nullable = false)
    @Comment("좋아요를 누른 사용자 PK")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    @Comment("좋아요를 받은 피드 PK")
    private Feed feed;


    private Like(Long userId, Feed feed) {
        this.userId = userId;
        this.feed = feed;
    }

    public static Like create(Long userId, Feed feed) {
        return new Like(userId, feed);
    }

}
