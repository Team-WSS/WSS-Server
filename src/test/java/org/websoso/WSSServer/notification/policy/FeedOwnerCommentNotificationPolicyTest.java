package org.websoso.WSSServer.notification.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.feed.feed.domain.Feed;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.BlockService;

@ExtendWith(MockitoExtension.class)
class FeedOwnerCommentNotificationPolicyTest {

    @InjectMocks
    private FeedOwnerCommentNotificationPolicy policy;

    @Mock
    private BlockService blockService;

    @Mock
    private User commentWriter;

    @Mock
    private Feed feed;

    @DisplayName("댓글 작성자와 피드 작성자가 차단 관계이면 알림을 보내지 않는다")
    @Test
    void excludesBlockedFeedOwner() {
        given(commentWriter.getUserId()).willReturn(1L);
        given(feed.getWriterId()).willReturn(2L);
        given(blockService.hasBlockRelation(2L, 1L)).willReturn(true);

        Optional<Long> result = policy.resolveRecipient(commentWriter, feed);

        assertThat(result).isEmpty();
    }

    @DisplayName("본인 피드에 댓글을 작성하면 피드 작성자 알림을 보내지 않는다")
    @Test
    void excludesCommentWriter() {
        given(commentWriter.getUserId()).willReturn(1L);
        given(feed.getWriterId()).willReturn(1L);

        Optional<Long> result = policy.resolveRecipient(commentWriter, feed);

        assertThat(result).isEmpty();
        then(blockService).shouldHaveNoInteractions();
    }
}
