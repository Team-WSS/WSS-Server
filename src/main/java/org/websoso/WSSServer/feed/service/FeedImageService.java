package org.websoso.WSSServer.feed.service;

import static org.websoso.WSSServer.domain.common.FeedImageType.FEED_THUMBNAIL;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.repository.FeedImageRepository;

@Service
@RequiredArgsConstructor
public class FeedImageService {

    private final FeedImageRepository feedImageRepository;

    @Transactional(readOnly = true)
    public String getThumbnailUrl(Feed feed) {
        Optional<FeedImage> thumbnailImage = feedImageRepository.findThumbnailFeedImageByFeedId(feed.getFeedId());
        return thumbnailImage.map(FeedImage::getUrl).orElse(null);
    }

    @Transactional(readOnly = true)
    public Integer getImageCount(Feed feed) {
        return feedImageRepository.countByFeedId(feed.getFeedId());
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getThumbnailUrlMap(List<Long> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return feedImageRepository.findByFeedIdIn(feedIds).stream()
                .filter(feedImage -> feedImage.getFeedImageType() == FEED_THUMBNAIL)
                .sorted(Comparator.comparing(FeedImage::getSequence))
                .collect(Collectors.toMap(
                        FeedImage::getFeedId,
                        FeedImage::getUrl,
                        (first, second) -> first
                ));
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getImageCountMap(List<Long> feedIds) {
        if (feedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return feedImageRepository.findByFeedIdIn(feedIds).stream()
                .collect(Collectors.groupingBy(
                        FeedImage::getFeedId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }
}
