package org.websoso.WSSServer.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.Like;
import org.websoso.WSSServer.feed.repository.LikeRepository;

@Service
@RequiredArgsConstructor
public class FeedLikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public boolean create(Long userId, Feed feed) {
        if (likeRepository.existsByUserIdAndFeed(userId, feed)) return false;

        try {
            likeRepository.save(Like.create(userId, feed));
            return true;
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 이미 생성되어 무결성을 위반한 경우 패스 처리
            return false;
        }
    }

    @Transactional
    public void deleteByFeedId(Long feedId) {
        likeRepository.deleteByFeedId(feedId);
    }

    @Transactional(readOnly = true)
    public long countByFeedId(Long feedId) {
        return likeRepository.countByFeed_FeedId(feedId);
    }

}
