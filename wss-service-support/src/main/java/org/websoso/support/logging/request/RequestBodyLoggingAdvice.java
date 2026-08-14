package org.websoso.support.logging.request;

import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.websoso.support.logging.masking.MaskedBody;
import org.websoso.support.logging.masking.SensitiveDataMasker;

/** 역직렬화된 요청 본문을 민감정보만 가린 구조화 로그로 기록한다. */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RequestBodyLoggingAdvice extends RequestBodyAdviceAdapter {

    private final SensitiveDataMasker sensitiveDataMasker;

    /** 모든 컨트롤러 요청 본문을 로깅 대상으로 지정한다. */
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /** 기존 요청 본문을 마스킹한 JSON 객체 필드로 변환해 기록한다. */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

        try {
            MaskedBody maskedBody = sensitiveDataMasker.mask(body);
            log.atInfo()
                    .addKeyValue("event", "API_REQUEST_BODY")
                    .addKeyValue("body", maskedBody.json())
                    .addKeyValue("truncated", maskedBody.truncated())
                    .log("API_REQUEST_BODY");
        } catch (Exception e) {
            log.atWarn()
                    .addKeyValue("event", "API_REQUEST_BODY_SERIALIZATION_FAILED")
                    .addKeyValue("error", e.getMessage())
                    .log("API_REQUEST_BODY_SERIALIZATION_FAILED");
        }

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}
