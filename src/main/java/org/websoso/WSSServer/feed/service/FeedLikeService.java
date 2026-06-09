package org.websoso.WSSServer.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public void deleteByFeedId(Long feedId) {
        likeRepository.deleteByFeedId(feedId);
    }

}
