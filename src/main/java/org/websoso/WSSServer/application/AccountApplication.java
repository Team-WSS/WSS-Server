package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.WITHDRAW;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.feed.comment.service.CommentServiceImpl;
import org.websoso.WSSServer.feed.feed.service.FeedServiceImpl;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessage;
import org.websoso.WSSServer.dto.user.WithdrawalRequest;
import org.websoso.WSSServer.auth.service.AppleService;
import org.websoso.WSSServer.auth.service.TokenService;
import org.websoso.WSSServer.auth.client.KakaoClient;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.domain.WithdrawalReason;
import org.websoso.WSSServer.user.message.UserDiscordMessageFormatter;
import org.websoso.WSSServer.user.repository.UserRepository;
import org.websoso.WSSServer.user.repository.WithdrawalReasonRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountApplication {
    private static final String KAKAO_PREFIX = "kakao";
    private static final String APPLE_PREFIX = "apple";

    private final WithdrawalReasonRepository withdrawalReasonRepository;
    private final DiscordMessageClient discordMessageClient;
    private final AppleService appleService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final KakaoClient kakaoClient;
    private final CommentServiceImpl commentService;
    private final FeedServiceImpl feedService;

    public void withdrawUser(User user, WithdrawalRequest withdrawalRequest) {
        unlinkSocialAccount(user);

        String messageContent = UserDiscordMessageFormatter.formatUserWithdrawMessage(
                user.getUserId(),
                user.getNickname(),
                withdrawalRequest.reason()
        );

        cleanupUserData(user.getUserId());

        discordMessageClient.sendDiscordWebhookMessage(
                DiscordWebhookMessage.of(messageContent, WITHDRAW));

        withdrawalReasonRepository.save(WithdrawalReason.create(withdrawalRequest.reason()));
    }

    private void unlinkSocialAccount(User user) {
        if (user.getSocialId().startsWith(KAKAO_PREFIX)) {
            kakaoClient.unlink(extractKakaoUserId(user.getSocialId()));
        } else if (user.getSocialId().startsWith(APPLE_PREFIX)) {
            appleService.unlinkFromApple(user);
        }
    }

    private String extractKakaoUserId(String socialId) {
        return socialId.replaceFirst(KAKAO_PREFIX + "_", "");
    }

    private void cleanupUserData(Long userId) {
        tokenService.deleteAllRefreshTokensByUserId(userId);
        commentService.updateWriterToUnknown(userId);
        feedService.updateWriterToUnknown(userId);
        userRepository.deleteById(userId);
    }

}
