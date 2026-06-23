package org.websoso.WSSServer.feed.feed.event;

import java.util.List;

public record FeedImageDeleteEvent(List<String> imageUrls) {
}
