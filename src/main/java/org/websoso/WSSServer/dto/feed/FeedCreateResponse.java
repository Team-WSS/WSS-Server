package org.websoso.WSSServer.dto.feed;

import org.websoso.WSSServer.domain.FeedImage;

import java.util.Comparator;
import java.util.List;

public record FeedCreateResponse(
        Integer imagesCount,
        List<String> imageUrls
) {
    public static FeedCreateResponse of(List<FeedImage> images) {
        return new FeedCreateResponse(
                images.size(),
                images.stream()
                        .sorted(Comparator.comparing(FeedImage::getSequence))
                        .map(FeedImage::getUrl)
                        .toList()
        );
    }
}