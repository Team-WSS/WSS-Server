package org.websoso.WSSServer.domain;


import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.websoso.WSSServer.domain.common.Flag;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Flag isCompleted;

    @Column(columnDefinition = "int default 0", nullable = false)
    private Integer novelRatingCount;

    @Column(columnDefinition = "float default 0.0", nullable = false)
    private Float novelRatingSum;

    @OneToOne(mappedBy = "novel", cascade = ALL)
    private NovelStatistics novelStatistics;

    @OneToMany(mappedBy = "novel")
    private List<UserNovel> userNovels = new ArrayList<>();

    @OneToMany(mappedBy = "novel")
    private List<Platform> platforms = new ArrayList<>();

    @OneToMany(mappedBy = "novel")
    private List<NovelGenre> novelGenres = new ArrayList<>();

    public void increaseNovelRatingCount() {
        this.novelRatingCount++;
    }

    public void decreaseNovelRatingCount() {
        this.novelRatingCount--;
    }

    public void increaseNovelRatingSum(float rating) {
        this.novelRatingSum += rating;
    }

    public void decreaseNovelRatingSum(float rating) {
        this.novelRatingSum -= rating;
    }

}
