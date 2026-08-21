package org.websoso.WSSServer.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.websoso.WSSServer.support.auth.TestBearerToken.accessToken;
import static org.websoso.common.exception.CustomCommonError.INVALID_REQUEST_PARAMETER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.websoso.WSSServer.application.UserBlockApplication;
import org.websoso.WSSServer.support.auth.AuthenticatedControllerTest;
import org.websoso.WSSServer.user.domain.User;
import org.websoso.WSSServer.user.service.UserService;

/**
 * {@code BlockController}의 파라미터 제약 위반이 다른 Controller와 같은 공통 코드로 응답하는지 검증한다.
 *
 * <p>이 Controller에는 클래스 수준 {@code @Validated}가 없어 {@code @Positive} 위반이
 * {@code ConstraintViolationException}이 아니라 {@code HandlerMethodValidationException}으로 실패한다.
 * 그 예외를 처리하지 않으면 같은 제약 위반이 API마다 다른 응답이 되고, 처리되지 않은 예외가
 * ERROR 디스패치로 넘어가 401 AUTH-001이 된다. 검증 방식과 무관하게 400 COMMON-003이어야 한다.
 */
@AuthenticatedControllerTest(BlockController.class)
class BlockControllerValidationTest {

    private static final Long USER_ID = 42L;
    private static final Long NOT_POSITIVE_ID = 0L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserBlockApplication userBlockApplication;

    @BeforeEach
    void setUp() {
        given(userService.getUserOrException(USER_ID)).willReturn(mock(User.class));
    }

    @DisplayName("양수가 아닌 차단 대상 ID로 차단하면 400 COMMON-003으로 응답한다")
    @Test
    void respondsWithCommonInvalidRequestParameterForNonPositiveRequestParam() throws Exception {
        mockMvc.perform(post("/blocks")
                        .queryParam("userId", NOT_POSITIVE_ID.toString())
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_PARAMETER.getCode()))
                .andExpect(jsonPath("$.message").isNotEmpty());

        then(userBlockApplication).should(never()).block(any(), any());
    }

    @DisplayName("양수가 아닌 차단 대상 ID로 v2 차단하면 400 COMMON-003으로 응답한다")
    @Test
    void respondsWithCommonInvalidRequestParameterForNonPositivePathVariable() throws Exception {
        mockMvc.perform(put("/blocks/users/{blockedUserId}/v2", NOT_POSITIVE_ID)
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_PARAMETER.getCode()));
    }

    @DisplayName("양수가 아닌 차단 ID로 차단을 해제하면 400 COMMON-003으로 응답한다")
    @Test
    void respondsWithCommonInvalidRequestParameterForNonPositiveBlockId() throws Exception {
        mockMvc.perform(delete("/blocks/{blockId}", NOT_POSITIVE_ID)
                        .with(accessToken(USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_PARAMETER.getCode()));
    }
}
