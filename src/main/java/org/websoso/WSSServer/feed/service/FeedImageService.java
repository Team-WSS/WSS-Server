package org.websoso.WSSServer.feed.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.domain.FeedImage;
import org.websoso.WSSServer.feed.repository.FeedImageRepository;

import java.util.Optional;

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
}
