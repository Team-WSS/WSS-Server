package org.websoso.WSSServer.support.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.websoso.WSSServer.auth.jwt.CustomAccessDeniedHandler;
import org.websoso.WSSServer.auth.jwt.CustomJwtAuthenticationEntryPoint;
import org.websoso.WSSServer.config.SecurityConfig;
import org.websoso.WSSServer.support.ControllerTestApplication;
import org.websoso.common.exception.ErrorResultWriter;

/**
 * 실제 {@code JwtAuthenticationFilter}, {@code JWTUtil}, {@code JwtKeyProvider},
 * {@code CustomUserArgumentResolver}를 통과하는 MockMvc Controller 테스트용 애너테이션.
 * 테스트 대상 Controller만 지정하면 되고, 토큰은 {@link TestBearerToken}으로 요청에 적용한다.
 *
 * <pre>
 * &#64;AuthenticatedControllerTest(AuthController.class)
 * class AuthControllerLogoutAuthenticationTest { ... }
 * </pre>
 *
 * 오류 본문을 쓰는 {@link ErrorResultWriter}는 웹 슬라이스가 자동으로 등록하지 않으므로 함께 가져온다.
 * 사용자 조회({@code UserService})와 Controller가 의존하는 Application/Service는
 * 테스트에서 {@code @MockBean}으로 격리한다. JWT Secret과 만료 시간은
 * {@link ControllerAuthTestConfig}가 제공하므로 application-*.yml이 필요 없다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebMvcTest
@ContextConfiguration(classes = ControllerTestApplication.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, CustomJwtAuthenticationEntryPoint.class, CustomAccessDeniedHandler.class,
        ErrorResultWriter.class, ControllerAuthTestConfig.class})
public @interface AuthenticatedControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] value() default {};
}
