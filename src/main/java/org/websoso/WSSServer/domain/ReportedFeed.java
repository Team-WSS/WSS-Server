package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportedFeed {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long reportedFeedId;

    @Column(columnDefinition = "tinyint default 0", nullable = false)
    private Byte spoilerCount;

    @Column(columnDefinition = "tinyint default 0", nullable = false)
    private Byte impertinenceCount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    private ReportedFeed(Feed feed) {
        this.feed = feed;
    }

    public static ReportedFeed create(Feed feed) {
        return new ReportedFeed(feed);
    }

    public void incrementSpoilerCount() {
        this.spoilerCount = (byte) (Optional.ofNullable(this.spoilerCount).orElse((byte) 0) + 1);
    }

}
