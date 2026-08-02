package org.websoso.WSSServer.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.TokenService;
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
    private User user;

    @DisplayName("회원 탈퇴 시 탈퇴한 사용자의 리프레시 토큰 정리를 토큰 서비스에 위임한다")
    @Test
    void delegatesRefreshTokenCleanupOnWithdraw() {
        given(user.getSocialId()).willReturn("kakao_1234");
        given(user.getUserId()).willReturn(USER_ID);

        accountApplication.withdrawUser(user, new WithdrawalRequest("탈퇴 사유"));

        then(tokenService).should().deleteAllRefreshTokensByUserId(USER_ID);
        then(userRepository).should().deleteById(USER_ID);
    }
}
