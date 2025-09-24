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
import org.springframework.web.util.ContentCachingRequestWrapper;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);

        long startTime = System.currentTimeMillis();
        log.info("[API REQ] {} {} | Parameters: {}",
                request.getMethod(), request.getRequestURI(), request.getQueryString());

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);

        try {
            filterChain.doFilter(requestWrapper, response);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[API RES] {} {} | Status: {} | Total Time: {}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), (endTime - startTime));

            MDC.clear();
        }
    }
}