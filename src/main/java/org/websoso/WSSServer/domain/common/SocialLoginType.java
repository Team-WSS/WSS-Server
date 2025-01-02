package org.websoso.WSSServer.domain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialLoginType {
    KAKAO("카카오"), APPLE("애플");

    private final String label;
    
}
