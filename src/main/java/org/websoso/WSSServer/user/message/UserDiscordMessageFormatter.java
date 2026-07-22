package org.websoso.WSSServer.user.message;

import static org.websoso.WSSServer.infrastructure.discord.DiscordMessageTemplate.USER_JOIN;
import static org.websoso.WSSServer.infrastructure.discord.DiscordMessageTemplate.USER_WITHDRAW;

import org.websoso.WSSServer.domain.common.SocialLoginType;
import org.websoso.WSSServer.infrastructure.discord.DiscordMessageTemplate;
import org.websoso.WSSServer.user.domain.User;

public class UserDiscordMessageFormatter {

    private UserDiscordMessageFormatter() {}

    public static String formatUserWithdrawMessage(Long userId, String userNickname, String reason) {
        return String.format(
                USER_WITHDRAW.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                userNickname,
                userId,
                reason
        );
    }

    public static String formatUserJoinMessage(User user, SocialLoginType socialLoginType) {
        return String.format(
                USER_JOIN.getTemplate(),
                DiscordMessageTemplate.getCurrentDateTime(),
                socialLoginType.getLabel(),
                user.getNickname(),
                user.getUserId()
        );
    }
}
