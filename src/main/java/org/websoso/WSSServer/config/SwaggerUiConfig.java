package org.websoso.WSSServer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.webjars.WebJarAssetLocator;
import org.webjars.WebJarAssetLocator.WebJarInfo;

/**
 * Swagger UI를 WebJar 번들로 직접 제공한다. 외부 CDN을 사용하지 않는다.
 * 표시할 명세는 런타임 분석 결과가 아니라 REST Docs 문서 테스트로 생성한
 * {@code classpath:/swagger-ui/openapi3.json}이며, {@code swagger-initializer.js}가 이 명세를 가리킨다.
 * <p>
 * 활성화 여부는 기존 배포 설정과의 호환을 위해 {@code springdoc.swagger-ui.enabled}를 그대로 사용한다.
 * 값이 없으면 제공하지 않는다.
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true")
public class SwaggerUiConfig implements WebMvcConfigurer {

    private static final String SWAGGER_UI_PATH_PATTERN = "/swagger-ui/**";
    private static final String SWAGGER_UI_ENTRY_PATH = "/swagger-ui.html";
    private static final String SWAGGER_UI_INDEX_PATH = "/swagger-ui/index.html";
    private static final String GENERATED_DOCS_LOCATION = "classpath:/swagger-ui/";
    private static final String WEB_JAR_NAME = "swagger-ui";
    private static final String WEB_JAR_LOCATION_FORMAT = "classpath:/META-INF/resources/webjars/%s/%s/";

    /**
     * 생성 명세와 초기화 스크립트를 담은 {@code classpath:/swagger-ui/}를 WebJar보다 먼저 조회해,
     * WebJar 기본 {@code swagger-initializer.js}를 프로젝트 설정으로 대체한다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(SWAGGER_UI_PATH_PATTERN)
                .addResourceLocations(GENERATED_DOCS_LOCATION, webJarLocation());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(SWAGGER_UI_ENTRY_PATH, SWAGGER_UI_INDEX_PATH);
    }

    private String webJarLocation() {
        WebJarInfo swaggerUi = new WebJarAssetLocator().getAllWebJars().get(WEB_JAR_NAME);
        if (swaggerUi == null) {
            throw new IllegalStateException("swagger-ui WebJar을 classpath에서 찾을 수 없습니다.");
        }
        return String.format(WEB_JAR_LOCATION_FORMAT, WEB_JAR_NAME, swaggerUi.getVersion());
    }
}
