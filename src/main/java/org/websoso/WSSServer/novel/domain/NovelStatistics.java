package org.websoso.WSSServer.novel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "novel_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NovelStatistics {

    @Id
    @Column(name = "novel_id")
    private Long novelId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false, precision = 4, scale = 3)
    @Comment("작품 평균 평점")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 1)
    @Comment("작품 평점 합계")
    private BigDecimal ratingSum = BigDecimal.ZERO;

    @Column(nullable = false)
    @Comment("작품 평점 등록 수")
    private Long ratingCount = 0L;

    @Column(nullable = false)
    @Comment("관심 등록 또는 하차 외 독서 상태인 사용자 수")
    private Long popularity = 0L;
}
