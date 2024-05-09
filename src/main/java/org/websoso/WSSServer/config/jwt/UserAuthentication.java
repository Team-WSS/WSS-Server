package org.websoso.WSSServer.config.jwt;

import java.util.Collection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class UserAuthentication extends UsernamePasswordAuthenticationToken {

    // 사용자 인증 객체 생성
    public UserAuthentication(Object principal, Object credentials,
                              Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}