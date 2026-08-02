package org.websoso.WSSServer.notification.policy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        Set<Long> blockedUserIds = new HashSet<>(blockService.findBlockRelationUserIds(commentWriterId));

        return feed.getComments().stream()
                .map(Comment::getUserId)
                .filter(userId -> !userId.equals(commentWriterId))
                .filter(userId -> !userId.equals(feedOwnerId))
                .filter(userId -> !blockedUserIds.contains(userId))
                .distinct()
                .toList();
    }
}
