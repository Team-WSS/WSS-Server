package org.websoso.WSSServer.feed.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.dto.feed.FeedInfo;
import org.websoso.WSSServer.dto.feed.UserFeedGetResponse;
import org.websoso.WSSServer.feed.domain.Feed;
import org.websoso.WSSServer.feed.repository.FeedInfoRow;
import org.websoso.WSSServer.feed.repository.FeedQueryRepository;
import org.websoso.WSSServer.feed.repository.UserFeedInfoRow;

@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final FeedQueryRepository feedQueryRepository;

    @Transactional(readOnly = true)
    public List<FeedInfo> findFeedInfoRows(List<Feed> feeds, Long userId) {
        List<Long> feedIds = feeds.stream()
                .map(Feed::getFeedId)
                .toList();

        Map<Long, FeedInfoRow> feedInfoRowMap = feedQueryRepository.findFeedInfoRows(feedIds, userId).stream()
                .collect(Collectors.toMap(FeedInfoRow::feedId, Function.identity()));

        return feedIds.stream()
                .map(feedInfoRowMap::get)
                .filter(Objects::nonNull)
                .map(FeedInfoRow::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserFeedGetResponse> findUserFeedRows(List<Feed> feeds, Long visitorId) {
        List<Long> feedIds = feeds.stream()
                .map(Feed::getFeedId)
                .toList();

        Map<Long, UserFeedInfoRow> userFeedInfoRowMap = feedQueryRepository.findUserFeedInfoRows(feedIds, visitorId).stream()
                .collect(Collectors.toMap(UserFeedInfoRow::feedId, Function.identity()));

        return feedIds.stream()
                .map(userFeedInfoRowMap::get)
                .filter(Objects::nonNull)
                .map(UserFeedInfoRow::toResponse)
                .toList();
    }

}
