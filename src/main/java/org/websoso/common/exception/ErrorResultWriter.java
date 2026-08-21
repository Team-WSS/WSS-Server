package org.websoso.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Servlet 필터·Security 핸들러가 {@link ErrorResult} 본문을 직접 써야 할 때 쓰는 공통 도구.
 *
 * <p>DispatcherServlet 안에서 발생한 오류는 {@code GlobalExceptionHandler}가 메시지 컨버터로 응답을 만들지만,
 * 인증 실패와 인가 실패는 Controller에 도달하기 전에 Security 필터 체인에서 끝난다. 그 자리에서는
 * 컨버터를 쓸 수 없어 상태 코드·Content-Type·본문을 직접 써야 하고, 그 절차가 여러 곳에 복제돼 있었다.
 * 오류 응답의 모양이 한 곳에서만 정해지도록 여기로 모은다.
 *
 * <p>{@link ICustomError} 정의만 받는다. 코드와 상태 코드를 호출부에서 정하지 않으므로
 * 같은 원인의 오류가 필터마다 다른 코드로 나가지 않는다.
 */
@Component
@RequiredArgsConstructor
public class ErrorResultWriter {

    private static final String CONTENT_TYPE = "application/json";
    private static final String CHARACTER_ENCODING = "UTF-8";

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, ICustomError error) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.setStatus(error.getStatusCode().value());
        response.getWriter().write(
                objectMapper.writeValueAsString(new ErrorResult(error.getCode(), error.getDescription())));
    }
}
