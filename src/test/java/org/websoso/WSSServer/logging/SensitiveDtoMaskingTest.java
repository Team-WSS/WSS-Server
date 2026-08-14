package org.websoso.WSSServer.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.websoso.WSSServer.auth.controller.dto.AppleLoginRequest;
import org.websoso.WSSServer.auth.controller.dto.AuthResponse;
import org.websoso.WSSServer.auth.controller.dto.LogoutRequest;
import org.websoso.WSSServer.dto.user.UserInfoGetResponse;
import org.websoso.WSSServer.notification.dto.FCMTokenRequest;
import org.websoso.support.logging.masking.SensitiveDataMasker;

/** 실제 DTO에 부착된 마스킹 어노테이션이 로그 직렬화에 적용되는지 검증한다. */
class SensitiveDtoMaskingTest {

    private final SensitiveDataMasker masker = new SensitiveDataMasker();

    @Test
    @DisplayName("애플 로그인 요청의 인증코드와 ID 토큰을 가린다")
    void masksAppleLoginCredentials() {
        JsonNode masked = masker.mask(new AppleLoginRequest("authorization-code", "id-token")).json();

        assertThat(masked.get("authorizationCode").asText()).isEqualTo("***(len=18)");
        assertThat(masked.get("idToken").asText()).isEqualTo("***(len=8)");
    }

    @Test
    @DisplayName("로그아웃 요청의 리프레시 토큰과 기기 식별자를 가린다")
    void masksLogoutCredentials() {
        JsonNode masked = masker.mask(new LogoutRequest("refresh-token", "device-identifier")).json();

        assertThat(masked.get("refreshToken").asText()).isEqualTo("***(len=13)");
        assertThat(masked.get("deviceIdentifier").asText()).isEqualTo("***(len=17)");
    }

    @Test
    @DisplayName("FCM 토큰 등록 요청의 토큰과 기기 식별자를 가린다")
    void masksFcmTokenRequest() {
        JsonNode masked = masker.mask(new FCMTokenRequest("fcm-token", "device-identifier")).json();

        assertThat(masked.get("fcmToken").asText()).isEqualTo("***(len=9)");
        assertThat(masked.get("deviceIdentifier").asText()).isEqualTo("***(len=17)");
    }

    @Test
    @DisplayName("내 정보 응답의 이메일·성별·출생연도를 가린다")
    void masksUserInfoResponse() {
        JsonNode masked = masker.mask(new UserInfoGetResponse("reader@websoso.kr", "MALE", 1998)).json();

        assertThat(masked.get("email").asText()).isEqualTo("re***@we***");
        assertThat(masked.get("gender").asText()).isEqualTo("***");
        assertThat(masked.get("birth").asText()).isEqualTo("***");
    }

    @Test
    @DisplayName("인증 응답의 액세스·리프레시 토큰을 가리고 가입 여부는 남긴다")
    void masksAuthResponseTokensOnly() {
        JsonNode masked = masker.mask(AuthResponse.of("access-token", "refresh-token", true)).json();

        assertThat(masked.get("Authorization").asText()).isEqualTo("***(len=12)");
        assertThat(masked.get("refreshToken").asText()).isEqualTo("***(len=13)");
        assertThat(masked.get("isRegister").asBoolean()).isTrue();
    }
}
