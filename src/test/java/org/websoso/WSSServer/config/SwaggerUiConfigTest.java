package org.websoso.WSSServer.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.websoso.WSSServer.application.AppVersionApplication;
import org.websoso.WSSServer.controller.AppVersionController;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code springdoc.swagger-ui.enabled=true}인 환경에서 Swagger UI가
 * 기존에 공개된 경로({@code /swagger-ui.html}, {@code /swagger-ui/**})로 실제 제공되는지 검증한다.
 * 명세 자체는 {@code ./gradlew apiDocs}가 {@code classpath:/swagger-ui/openapi3.json}에 만들며,
 * 테스트에서는 같은 위치의 고정 파일로 경로가 제공되는지 확인한다.
 */
@TestPropertySource(properties = "springdoc.swagger-ui.enabled=true")
@AuthenticatedControllerTest(AppVersionController.class)
class SwaggerUiConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppVersionApplication appVersionApplication;

    @MockBean
    private UserService userService;

    @DisplayName("Swagger UI 진입 경로는 인증 없이 index로 리다이렉트된다")
    @Test
    void redirectsEntryPathToIndex() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @DisplayName("Swagger UI 번들은 인증 없이 제공된다")
    @Test
    void servesSwaggerUiBundleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/swagger-ui-bundle.js"))
                .andExpect(status().isOk());
    }

    @DisplayName("초기화 스크립트는 WebJar 기본값이 아니라 생성된 명세를 가리킨다")
    @Test
    void initializerPointsToGeneratedSpecification() throws Exception {
        mockMvc.perform(get("/swagger-ui/swagger-initializer.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"./openapi3.json\"")));
    }

    @DisplayName("생성된 OpenAPI 명세는 인증 없이 제공된다")
    @Test
    void servesGeneratedSpecificationWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/openapi3.json"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"openapi\"")));
    }
}
