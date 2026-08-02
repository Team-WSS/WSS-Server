package org.websoso.WSSServer.user.message;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.websoso.WSSServer.domain.common.SocialLoginType;
import org.websoso.WSSServer.user.domain.User;

class UserDiscordMessageFormatterTest {

    @Nested
    @DisplayName("회원 탈퇴 메시지")
    class UserWithdrawMessage {

        @Test
        void formatsUserWithdrawMessage() {
            String result = UserDiscordMessageFormatter.formatUserWithdrawMessage(
                    1L,
                    "탈퇴하는사용자",
                    "개인사정"
            );

            assertThat(result)
                    .contains("탈퇴하는사용자")
                    .contains("개인사정");
        }
    }

    @Nested
    @DisplayName("회원 가입 메시지")
    class UserJoinMessage {

        @Test
        void formatsUserJoinMessage() {
            User user = createUser(1L, "새로운사용자");

            String result = UserDiscordMessageFormatter.formatUserJoinMessage(user, SocialLoginType.KAKAO);

            assertThat(result)
                    .contains("카카오")
                    .contains("새로운사용자");
        }
    }

    private User createUser(Long userId, String nickname) {
        User user = User.createBySocial("social-" + userId, nickname, null);
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }
}
