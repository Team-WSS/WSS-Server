package org.websoso.WSSServer.dto.feed;

import java.util.List;

public record FeedImageDeleteEvent(List<String> imageUrls) {
}
