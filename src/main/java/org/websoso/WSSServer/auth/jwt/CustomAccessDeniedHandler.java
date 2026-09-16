package org.websoso.WSSServer.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.websoso.common.exception.CustomCommonError;
import org.websoso.common.exception.ErrorResult;
import org.websoso.common.exception.ErrorResultWriter;

/**
 * 인증은 됐지만 권한이 없는 요청의 응답을 정의한다.
 * 상태 코드만 있는 빈 응답은 클라이언트가 인증 실패와 인가 실패를 본문으로 구분할 수 없게 하므로,
 * 인증 오류와 같은 {@link ErrorResult} 형식으로 {@link CustomCommonError#ACCESS_DENIED}를 내려준다.
 *
 * <p>어느 도메인의 API에서 막혔는지는 여기서 알 수 없다. 특정 리소스의 소유권 위반처럼 도메인이
 * 판단하는 인가 실패는 도메인 코드로 응답하고, 여기에는 도메인과 무관한 일반 인가 실패만 들어온다.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResultWriter errorResultWriter;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorResultWriter.write(response, CustomCommonError.ACCESS_DENIED);
    }
}
