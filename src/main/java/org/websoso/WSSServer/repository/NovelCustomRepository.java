package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.websoso.WSSServer.domain.Genre;
import org.websoso.WSSServer.domain.Keyword;
import org.websoso.WSSServer.domain.Novel;

public interface NovelCustomRepository {

    Page<Novel> findFilteredNovels(Pageable pageable, List<Genre> genres, Boolean isCompleted, Float novelRating,
                                   List<Keyword> keywords);

    Page<Novel> findSearchedNovels(Pageable pageable, String query);
}
