package org.websoso.support.logging.response;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** 응답 본문의 구조만 요약해 구조화 로그로 기록한다. */
@Slf4j
@RestControllerAdvice
public class ResponseBodyLoggingAdvice implements ResponseBodyAdvice<Object> {

    /** 서비스 트래픽이 아니어서 기록 가치가 없는 경로다. */
    private static final List<String> EXCLUDED_PATH_PREFIXES = List.of("/actuator", "/swagger-ui", "/v3/api-docs");

    private final ResponseBodySummarizer responseBodySummarizer = new ResponseBodySummarizer();

    /** 모든 컨트롤러 응답 본문을 로깅 대상으로 지정한다. */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /** 응답 본문을 그대로 통과시키고 구조 요약만 기록한다. */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (isExcluded(request)) {
            return body;
        }

        try {
            log.atInfo()
                    .addKeyValue("event", "API_RESPONSE_BODY")
                    .addKeyValue("body", responseBodySummarizer.summarize(body))
                    .log("API_RESPONSE_BODY");
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("event", "API_RESPONSE_BODY_SUMMARY_FAILED")
                    .addKeyValue("error", e.getMessage())
                    .log("API_RESPONSE_BODY_SUMMARY_FAILED");
        }

        return body;
    }

    /** 로깅 대상에서 제외할 경로인지 판단한다. */
    private boolean isExcluded(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return EXCLUDED_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
