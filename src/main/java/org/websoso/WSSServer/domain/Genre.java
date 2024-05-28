package org.websoso.WSSServer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long genreId;

    @Column(columnDefinition = "varchar(5)", nullable = false)
    private String genreName;

    @Column(columnDefinition = "text", nullable = false)
    private String genreBadge;

    @OneToMany(mappedBy = "genre")
    private List<NovelGenre> novelGenres = new ArrayList<>();
}
