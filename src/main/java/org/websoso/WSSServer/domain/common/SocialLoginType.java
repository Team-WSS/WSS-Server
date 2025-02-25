package org.websoso.WSSServer.domain.common;

import static org.websoso.WSSServer.exception.error.CustomAuthError.UNSUPPORTED_SOCIAL_LOGIN_TYPE;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.websoso.WSSServer.exception.exception.CustomAuthException;

@Getter
@AllArgsConstructor
public enum SocialLoginType {
    KAKAO("카카오", "kakao"),
    APPLE("애플", "apple");

    private final String label;
    private final String prefix;

    public static SocialLoginType fromSocialId(String socialId) {
        for (SocialLoginType type : values()) {
            if (socialId.startsWith(type.getPrefix())) {
                return type;
            }
        }
        throw new CustomAuthException(UNSUPPORTED_SOCIAL_LOGIN_TYPE, "unsupported social login type");
    }
}
