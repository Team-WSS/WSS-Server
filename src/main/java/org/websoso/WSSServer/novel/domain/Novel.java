package org.websoso.WSSServer.novel.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    private List<UserNovel> userNovels = new ArrayList<>();

    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    private List<NovelGenre> novelGenres = new ArrayList<>();

}
