package org.websoso.support.logging.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RequestBodyLoggingAdvice extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper;
    private final HttpServletRequest httpServletRequest;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

        try {
            String bodyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            log.info("[REQ BODY] {}", bodyJson);
        } catch (Exception e) {
            log.warn("Failed to log request body: {}", e.getMessage());
        }

        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}
