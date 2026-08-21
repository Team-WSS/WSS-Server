package org.websoso.WSSServer.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.websoso.WSSServer.exception.error.CustomAuthError;
import org.websoso.common.exception.ErrorResult;
import org.websoso.common.exception.ErrorResultWriter;

/**
 * 인증이 필요한 요청이 인증 정보 없이 도달했을 때의 응답을 정의한다.
 * {@code JwtAuthenticationFilter}는 Bearer 토큰이 있을 때만 검증 결과를 응답으로 쓰므로,
 * Authorization 헤더가 없거나 Bearer 형식이 아닌 요청은 여기로 들어온다.
 * 상태 코드만 있는 빈 응답 대신 필터와 같은 형식의 {@link ErrorResult} 본문을 내려준다.
 */
@Component
@RequiredArgsConstructor
public class CustomJwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResultWriter errorResultWriter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorResultWriter.write(response, CustomAuthError.INVALID_TOKEN);
    }

}
