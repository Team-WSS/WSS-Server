package org.websoso.WSSServer.domain;


import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.domain.common.FeedImageType;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedImage {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Long feedImageId;

    @Column(nullable = false)
    String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    FeedImageType feedImageType;

    @Column(columnDefinition = "tinyint", nullable = false)
    Integer sequence;

    @Column(name = "feed_id", nullable = false)
    Long feedId;

    private FeedImage(String url, FeedImageType feedImageType, int sequence) {
        this.url = url;
        this.feedImageType = feedImageType;
        this.sequence = sequence;
    }

    public static FeedImage createThumbnail(String url) {
        return new FeedImage(url, FeedImageType.FEED_THUMBNAIL, 0);
    }

    public static FeedImage createCommon(String url, int sequence) {
        return new FeedImage(url, FeedImageType.FEED_COMMON, sequence);
    }
}
