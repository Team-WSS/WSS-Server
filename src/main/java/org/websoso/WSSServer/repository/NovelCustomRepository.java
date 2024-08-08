package org.websoso.WSSServer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.websoso.WSSServer.domain.Novel;

public interface NovelCustomRepository {

    Page<Novel> findSearchedNovels(Pageable pageable, String query);

}
