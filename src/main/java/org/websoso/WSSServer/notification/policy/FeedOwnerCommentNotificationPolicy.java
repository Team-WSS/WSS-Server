package org.websoso.WSSServer.notification.policy;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@Component
@RequiredArgsConstructor
public class FeedOwnerCommentNotificationPolicy {

    private final BlockService blockService;

    public Optional<Long> resolveRecipient(User commentWriter, Feed feed) {
        Long feedOwnerId = feed.getWriterId();
        Long commentWriterId = commentWriter.getUserId();

        if (commentWriterId.equals(feedOwnerId)) {
            return Optional.empty();
        }

        if (blockService.hasBlockRelation(feedOwnerId, commentWriterId)) {
            return Optional.empty();
        }

        return Optional.of(feedOwnerId);
    }
}
