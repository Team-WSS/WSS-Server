package org.websoso.WSSServer.notification.policy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@Component
@RequiredArgsConstructor
public class CommenterCommentNotificationPolicy {

    private final BlockService blockService;

    public List<Long> resolveRecipients(User commentWriter, Feed feed) {
        Long commentWriterId = commentWriter.getUserId();
        Long feedOwnerId = feed.getWriterId();

        return feed.getComments().stream()
                .map(Comment::getUserId)
                .filter(userId -> !userId.equals(commentWriterId))
                .filter(userId -> !userId.equals(feedOwnerId))
                .filter(userId -> !blockService.exists(userId, commentWriterId))
                .filter(userId -> !blockService.exists(userId, feedOwnerId))
                .distinct()
                .toList();
    }
}
