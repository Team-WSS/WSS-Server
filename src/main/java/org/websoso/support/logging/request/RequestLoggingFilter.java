package org.websoso.support.logging.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 요청과 응답 정보를 동일한 traceId의 구조화 로그로 기록한다. */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /** JWT 인증 필터가 MDC에 저장하는 사용자 ID 키다. */
    public static final String USER_ID = "userId";
    private static final String TRACE_ID = "traceId";

    /** 요청 정보와 처리 결과를 CloudWatch에서 검색 가능한 JSON 필드로 기록한다. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);

        long startTime = System.currentTimeMillis();
        log.atInfo()
                .addKeyValue("event", "API_REQUEST")
                .addKeyValue("method", request.getMethod())
                .addKeyValue("uri", request.getRequestURI())
                .addKeyValue("query", request.getQueryString())
                .log("API_REQUEST");

        try {
            filterChain.doFilter(request, response);
        } finally {
            long endTime = System.currentTimeMillis();
            log.atInfo()
                    .addKeyValue("event", "API_RESPONSE")
                    .addKeyValue("method", request.getMethod())
                    .addKeyValue("uri", request.getRequestURI())
                    .addKeyValue("status", response.getStatus())
                    .addKeyValue("durationMs", endTime - startTime)
                    .log("API_RESPONSE");

            MDC.clear();
        }
    }
}
