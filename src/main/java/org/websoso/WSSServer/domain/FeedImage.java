package org.websoso.WSSServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.domain.common.FeedImageType;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long feedImageId;

    @Column(nullable = false)
    String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    FeedImageType feedImageType;

    @Column(columnDefinition = "tinyint", nullable = false)
    Integer sequence;

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
