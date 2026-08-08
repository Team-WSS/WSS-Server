package org.websoso.WSSServer.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.collection.service.CollectionService;
import org.websoso.WSSServer.dto.user.WithdrawalRequest;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.user.repository.WithdrawalReasonRepository;

@ExtendWith(MockitoExtension.class)
class AccountApplicationTest {

    private static final Long USER_ID = 1L;
    private static final String KAKAO_SOCIAL_ID = "kakao_1234";
    private static final String APPLE_SOCIAL_ID = "apple_1234";

    @InjectMocks
    private AccountApplication accountApplication;

    @Mock
    private WithdrawalReasonRepository withdrawalReasonRepository;

    @Mock
    private DiscordMessageClient discordMessageClient;

    @Mock
    private AppleService appleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private CommentServiceImpl commentService;

    @Mock
    private FeedServiceImpl feedService;

    @Mock
    private CollectionService collectionService;

    @Mock
    private User user;

    @DisplayName("회원 탈퇴 시 탈퇴한 사용자의 리프레시 토큰 정리를 토큰 서비스에 위임한다")
    @Test
    void delegatesRefreshTokenCleanupOnWithdraw() {
        givenWithdrawingUser(KAKAO_SOCIAL_ID);

        accountApplication.withdrawUser(user, new WithdrawalRequest("탈퇴 사유"));

        then(tokenService).should().deleteAllRefreshTokensByUserId(USER_ID);
        then(userRepository).should().deleteById(USER_ID);
    }

    @DisplayName("회원 탈퇴 시 소유 컬렉션의 소유자를 넘긴 뒤 사용자를 삭제한다")
    @Test
    void movesOwnedCollectionsToUnknownUserBeforeDeletingUser() {
        givenWithdrawingUser(KAKAO_SOCIAL_ID);

        accountApplication.withdrawUser(user, new WithdrawalRequest("탈퇴 사유"));

        InOrder inOrder = inOrder(collectionService, userRepository);
        inOrder.verify(collectionService).updateOwnerToUnknown(USER_ID);
        inOrder.verify(userRepository).deleteById(USER_ID);
    }

    @DisplayName("애플 계정도 동일하게 컬렉션 소유자를 넘긴 후 사용자를 삭제한다")
    @Test
    void movesOwnedCollectionsToUnknownUserForAppleUser() {
        givenWithdrawingUser(APPLE_SOCIAL_ID);

        accountApplication.withdrawUser(user, new WithdrawalRequest("탈퇴 사유"));

        then(appleService).should().unlinkFromApple(user);
        InOrder inOrder = inOrder(collectionService, userRepository);
        inOrder.verify(collectionService).updateOwnerToUnknown(USER_ID);
        inOrder.verify(userRepository).deleteById(USER_ID);
    }

    @DisplayName("컬렉션 소유자 이관은 피드·댓글 작성자 익명화와 같은 정리 단계에서 이뤄진다")
    @Test
    void movesCollectionOwnerAlongsideFeedAndCommentWriters() {
        givenWithdrawingUser(KAKAO_SOCIAL_ID);

        accountApplication.withdrawUser(user, new WithdrawalRequest("탈퇴 사유"));

        InOrder inOrder = inOrder(commentService, feedService, collectionService, userRepository);
        inOrder.verify(commentService).updateWriterToUnknown(USER_ID);
        inOrder.verify(feedService).updateWriterToUnknown(USER_ID);
        inOrder.verify(collectionService).updateOwnerToUnknown(USER_ID);
        inOrder.verify(userRepository).deleteById(USER_ID);
    }

    @DisplayName("컬렉션 정리가 실패하면 사용자를 삭제하지 않는다")
    @Test
    void doesNotDeleteUserWhenCollectionCleanupFails() {
        givenWithdrawingUser(KAKAO_SOCIAL_ID);
        willThrow(new IllegalStateException("cleanup failed"))
                .given(collectionService).updateOwnerToUnknown(USER_ID);

        assertThatThrownBy(() -> accountApplication.withdrawUser(user, new WithdrawalRequest("탈퇴 사유")))
                .isInstanceOf(IllegalStateException.class);

        then(userRepository).should(never()).deleteById(any());
    }

    private void givenWithdrawingUser(String socialId) {
        given(user.getSocialId()).willReturn(socialId);
        given(user.getUserId()).willReturn(USER_ID);
    }
}
