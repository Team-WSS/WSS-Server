package org.websoso.WSSServer.domain;

import static jakarta.persistence.GenerationType.IDENTITY;
import static org.websoso.WSSServer.exception.novelStatistics.NovelStatisticsErrorCode.INVALID_NOVEL_FEED_COUNT;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.exception.novelStatistics.exception.InvalidNovelStatisticsException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NovelStatistics {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(nullable = false)
    private Long novelStatisticsId;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer novelFeedCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer watchingCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer interestCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer watchedCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer quitCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer universeCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer vibeCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer materialCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer charactersCount;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer relationshipCount;

    @OneToOne
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    public void decreaseNovelFeedCount() {
        if (this.novelFeedCount <= 0) {
            throw new InvalidNovelStatisticsException(INVALID_NOVEL_FEED_COUNT, "invalid novel feed count");
        }

        this.novelFeedCount--;
    }

}
