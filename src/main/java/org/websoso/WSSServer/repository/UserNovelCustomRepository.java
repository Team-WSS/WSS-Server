package org.websoso.WSSServer.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

public interface UserNovelCustomRepository {

    UserNovelCountGetResponse findUserNovelStatistics(User user);

    List<Long> findTodayPopularNovelsId(Pageable pageable);
}
