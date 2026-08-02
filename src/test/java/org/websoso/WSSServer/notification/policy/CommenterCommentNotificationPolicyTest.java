package org.websoso.WSSServer.notification.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.feed.comment.domain.Comment;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@ExtendWith(MockitoExtension.class)
class CommenterCommentNotificationPolicyTest {

    @InjectMocks
    private CommenterCommentNotificationPolicy policy;

    @Mock
    private BlockService blockService;

    @Mock
    private User commentWriter;

    @Mock
    private Feed feed;

    @DisplayName("댓글 작성자와 차단 관계인 댓글 참여자를 알림 대상에서 제외한다")
    @Test
    void excludesBlockedCommentParticipants() {
        Comment writerComment = comment(1L);
        Comment ownerComment = comment(2L);
        Comment allowedComment = comment(3L);
        Comment blockedComment = comment(4L);
        Comment duplicatedAllowedComment = comment(3L);

        given(commentWriter.getUserId()).willReturn(1L);
        given(feed.getWriterId()).willReturn(2L);
        given(feed.getComments()).willReturn(List.of(
                writerComment,
                ownerComment,
                allowedComment,
                blockedComment,
                duplicatedAllowedComment
        ));
        given(blockService.findBlockRelationUserIds(1L)).willReturn(List.of(4L));

        List<Long> result = policy.resolveRecipients(commentWriter, feed);

        assertThat(result).containsExactly(3L);
        then(blockService).should().findBlockRelationUserIds(1L);
    }

    private Comment comment(Long userId) {
        Comment comment = org.mockito.Mockito.mock(Comment.class);
        given(comment.getUserId()).willReturn(userId);
        return comment;
    }
}
