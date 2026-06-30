package org.websoso.WSSServer.novel.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.websoso.WSSServer.library.domain.UserNovel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Novel {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long novelId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "text", nullable = false)
    private String novelImage;

    @Column(columnDefinition = "text", nullable = false)
    private String novelDescription;

    @Column(columnDefinition = "Boolean default false", nullable = false)
    private Boolean isCompleted;

    @Column(nullable = false, columnDefinition = "decimal(4, 3) default 0.000")
    @Comment("작품 평균 평점")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(nullable = false, columnDefinition = "decimal(19, 1) default 0.0")
    @Comment("작품 평점 합계")
    private BigDecimal ratingSum = BigDecimal.ZERO;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    @Comment("작품 평점 등록 수")
    private Long ratingCount = 0L;

    @Column(nullable = false, columnDefinition = "bigint default 0")
    @Comment("관심 등록 또는 하차 외 독서 상태인 사용자 수")
    private Long popularity = 0L;

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    private List<UserNovel> userNovels = new ArrayList<>();

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    private List<NovelGenre> novelGenres = new ArrayList<>();

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    private List<NovelPlatform> novelPlatforms = new ArrayList<>();

    public String getFirstGenreName(){
        return novelGenres.stream()
                .findFirst()
                .map(novelGenre -> novelGenre.getGenre().getGenreName()).orElse(null);
    }

}
