package org.websoso.WSSServer.feed.repository;

import java.util.List;
import org.websoso.WSSServer.feed.domain.PopularFeed;

public interface PopularFeedCustomRepository {

    /**
     * 로그인 사용자가 조회 가능한 인기 피드를 조회한다.
     * 차단한 사용자 및 차단당한 사용자의 피드는 제외한다.
     */
    List<PopularFeed> findPopularFeedsForMember(Long userId, int size);

    /**
     * 비회원이 조회 가능한 인기 피드를 조회한다.
     */
    List<PopularFeed> findPopularFeedsForGuest(int size);
}
