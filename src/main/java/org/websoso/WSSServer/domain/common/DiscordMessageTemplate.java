package org.websoso.WSSServer.domain.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscordMessageTemplate {

    FEED_REPORT(
            "```[%s] 피드 %s 신고가 접수되었습니다.\n\n"
                    + "[신고한 유저]\n"
                    + "유저 아이디 : %d\n"
                    + "유저 닉네임 : %s\n\n"
                    + "[신고된 피드 작성자]\n"
                    + "유저 아이디 : %d\n"
                    + "유저 닉네임 : %s\n\n"
                    + "[신고된 피드 내용]\n%s\n\n"
                    + "[신고 횟수]\n총 신고 횟수 %d회.\n"
                    + "%s\n```"),

    COMMENT_REPORT(
            "```[%s] 피드 댓글 %s 신고가 접수되었습니다.\n\n"
                    + "[신고한 유저]\n"
                    + "유저 아이디 : %d\n"
                    + "유저 닉네임 : %s\n\n"
                    + "[신고된 댓글 작성자]\n"
                    + "유저 아이디 : %d\n"
                    + "유저 닉네임 : %s\n\n"
                    + "[원글 내용]\n%s\n\n"
                    + "[신고된 댓글 내용]\n%s\n\n"
                    + "[신고 횟수]\n총 신고 횟수 %d회.\n"
                    + "%s\n```"),

    USER_WITHDRAW(
            "```[%s] 사용자가 탈퇴하였습니다.\n\n"
                    + "[탈퇴한 사용자]\n"
                    + "유저 아이디 : %d\n"
                    + "유저 닉네임 : %s\n\n"
                    + "[탈퇴 사유]\n%s\n\n```"),

    USER_JOIN(
            "```[%s] 새로운 사용자가 가입하였습니다.\n\n"
                    + "[가입한 사용자]\n"
                    + "로그인 방식 : %s\n"
                    + "유저 아이디 : %d\n"
                    + "가입을 환영합니다!\n\n```");

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private final String template;

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }
}

