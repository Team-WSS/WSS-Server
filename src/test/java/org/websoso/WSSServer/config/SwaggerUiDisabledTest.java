package org.websoso.WSSServer.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * 배포 환경별 {@code springdoc.swagger-ui.enabled} 계약을 유지하는지 검증한다.
 * prod처럼 값이 false면 Swagger UI와 명세를 제공하지 않는다.
 */
@TestPropertySource(properties = "springdoc.swagger-ui.enabled=false")
@AuthenticatedControllerTest(AppVersionController.class)
class SwaggerUiDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppVersionApplication appVersionApplication;

    @MockBean
    private UserService userService;

    @DisplayName("swagger-ui가 비활성이면 UI와 명세를 제공하지 않는다")
    @Test
    void doesNotServeSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/swagger-ui/openapi3.json"))
                .andExpect(status().isNotFound());
    }
}
