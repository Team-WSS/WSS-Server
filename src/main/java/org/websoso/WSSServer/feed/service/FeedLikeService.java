package org.websoso.WSSServer.feed.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.Like;
import org.websoso.WSSServer.feed.repository.FeedCountProjection;
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
    public void delete(Long userId, Feed feed) {
        likeRepository.deleteByUserIdAndFeed(userId, feed);
    }

    @Transactional
    public void deleteByFeedId(Long feedId) {
        likeRepository.deleteByFeedId(feedId);
    }

    @Transactional(readOnly = true)
    public long countByFeedId(Long feedId) {
        return likeRepository.countByFeed_FeedId(feedId);
    }

    @Transactional(readOnly = true)
    public boolean isUserLikedFeed(Long userId, Feed feed) {
        return likeRepository.existsByUserIdAndFeed(userId, feed);
    }

    @Transactional(readOnly = true)
    public List<Long> findLikedFeedIds(Long userId, List<Long> feedIds) {
        if (userId == null || feedIds.isEmpty()) {
            return Collections.emptyList();
        }

        return likeRepository.findLikedFeedIds(userId, feedIds);
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> countByFeedIds(List<Long> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return likeRepository.countByFeedIds(feedIds).stream()
                .collect(Collectors.toMap(
                        FeedCountProjection::getFeedId,
                        projection -> projection.getCount().intValue()
                ));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<Long>> findLikerUserIdsByFeedIds(List<Long> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return likeRepository.findByFeedFeedIdIn(feedIds).stream()
                .collect(Collectors.groupingBy(
                        like -> like.getFeed().getFeedId(),
                        Collectors.mapping(Like::getUserId, Collectors.toList())
                ));
    }

}
