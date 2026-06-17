package org.websoso.WSSServer.application;

import static org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessageType.WITHDRAW;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageClient;
import org.websoso.WSSServer.infrastructure.discord.DiscordWebhookMessage;
import org.websoso.WSSServer.dto.user.WithdrawalRequest;
import org.websoso.WSSServer.feed.comment.repository.CommentRepository;
import org.websoso.WSSServer.feed.feed.repository.FeedRepository;
import org.websoso.WSSServer.oauth2.service.AppleService;
import org.websoso.WSSServer.oauth2.service.KakaoService;
import org.websoso.WSSServer.oauth2.repository.RefreshTokenRepository;
import org.websoso.WSSServer.notification.service.MessageFormatter;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.domain.WithdrawalReason;
import org.websoso.WSSServer.user.event.WithdrawUserEvent;
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
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoService kakaoService;

    private final ApplicationEventPublisher eventPublisher;

    public void withdrawUser(User user, WithdrawalRequest withdrawalRequest) {
        unlinkSocialAccount(user);

        String messageContent = MessageFormatter.formatUserWithdrawMessage(user.getUserId(), user.getNickname(),
                withdrawalRequest.reason());

        cleanupUserData(user.getUserId());

        discordMessageClient.sendDiscordWebhookMessage(
                DiscordWebhookMessage.of(messageContent, WITHDRAW));

        withdrawalReasonRepository.save(WithdrawalReason.create(withdrawalRequest.reason()));

        eventPublisher.publishEvent(
                WithdrawUserEvent.of(user.getUserId())
        );
    }

    private void unlinkSocialAccount(User user) {
        if (user.getSocialId().startsWith(KAKAO_PREFIX)) {
            kakaoService.unlinkFromKakao(user);
        } else if (user.getSocialId().startsWith(APPLE_PREFIX)) {
            appleService.unlinkFromApple(user);
        }
    }

    private void cleanupUserData(Long userId) {
        refreshTokenRepository.deleteAll(refreshTokenRepository.findAllByUserId(userId));
        commentRepository.updateUserToUnknown(userId);
        userRepository.deleteById(userId);
    }

}
