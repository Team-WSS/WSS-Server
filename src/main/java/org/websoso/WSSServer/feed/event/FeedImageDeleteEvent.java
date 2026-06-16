package org.websoso.WSSServer.feed.event;

import java.util.List;

public record FeedImageDeleteEvent(List<String> imageUrls) {
}
