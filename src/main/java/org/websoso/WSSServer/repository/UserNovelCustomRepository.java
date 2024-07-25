package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserNovelCustomRepository {

    List<Long> findTodayPopularNovelsId(Pageable pageable);
}
