package org.websoso.WSSServer.domain.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscordMessageTemplate {

    FEED_REPORT(
            "```[%s] 🚨 수다 %s 신고 접수 🚨\n\n"
                    + "📌 [신고 정보]\n"
                    + "- 신고한 유저: %s (ID: %d)\n\n"
                    + "💬 [신고된 수다]\n"
                    + "- 작성자: %s (ID: %d)\n"
                    + "- 내용: %s\n\n"
                    + "🔔 [처리 상태]\n"
                    + "- 신고 횟수: 총 %d회\n"
                    + "- %s\n```"),

    COMMENT_REPORT(
            "```[%s] 🚨 수다 댓글 %s 신고 접수 🚨\n\n"
                    + "📌 [신고 정보]\n"
                    + "- 신고한 유저: %s (ID: %d)\n\n"
                    + "💬 [신고된 댓글]\n"
                    + "- 작성자: %s (ID: %d)\n"
                    + "- 내용: %s\n\n"
                    + "📄 [원글 정보]\n"
                    + "- 작성자: %s (ID: %d)\n"
                    + "- 내용: %s\n\n"
                    + "🔔 [처리 상태] \n"
                    + "- 신고 횟수: 총 %d회\n"
                    + "- %s\n```"),

    USER_WITHDRAW(
            "```[%s] ❌ 사용자가 탈퇴하였습니다 ❌\n\n"
                    + "[탈퇴한 사용자]\n"
                    + "- 닉네임: %s (ID: %d)\n"
                    + "- 탈퇴 사유: %s\n\n```"),

    USER_JOIN(
            "```[%s] 🎉 새로운 사용자가 가입하였습니다 🎉\n\n"
                    + "[가입한 사용자]\n"
                    + "- 로그인 방식: %s\n"
                    + "- ID: %d\n"
                    + "환영합니다!\n\n```");

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private final String template;

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }
}
