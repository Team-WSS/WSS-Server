package org.websoso.WSSServer.repository;

import org.websoso.WSSServer.domain.User;
import org.websoso.WSSServer.dto.user.UserNovelCountGetResponse;

public interface UserNovelCustomRepository {

    UserNovelCountGetResponse findUserNovelStatistics(User user);
}
