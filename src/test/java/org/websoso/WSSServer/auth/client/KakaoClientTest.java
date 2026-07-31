package org.websoso.WSSServer.auth.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.INVALID_KAKAO_ACCESS_TOKEN;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.KAKAO_REQUEST_FAILED;
import static org.websoso.WSSServer.exception.error.CustomKakaoError.KAKAO_SERVER_ERROR;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.websoso.WSSServer.auth.client.dto.KakaoUserInfo;
import org.websoso.WSSServer.exception.exception.CustomKakaoException;

class KakaoClientTest {

    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";
    private static final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";
    private static final String ADMIN_KEY = "test-admin-key";
    private static final String ACCESS_TOKEN = "kakao-access-token";
    private static final String PROVIDER_USER_ID = "1234567890";
    private static final String EXPECTED_FORM_BODY = "target_id_type=user_id&target_id=" + PROVIDER_USER_ID;

    private MockRestServiceServer mockServer;
    private KakaoClient kakaoClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        kakaoClient = new KakaoClient(builder.build());
        ReflectionTestUtils.setField(kakaoClient, "kakaoUserInfoUrl", USER_INFO_URL);
        ReflectionTestUtils.setField(kakaoClient, "kakaoLogoutUrl", LOGOUT_URL);
        ReflectionTestUtils.setField(kakaoClient, "kakaoUnlinkUrl", UNLINK_URL);
        ReflectionTestUtils.setField(kakaoClient, "kakaoAdminKey", ADMIN_KEY);
    }

    @DisplayName("사용자 정보 조회에 성공하면 카카오 사용자 정보를 반환한다")
    @Test
    void getUserInfo_success() {
        mockServer.expect(requestTo(USER_INFO_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess("""
                        {
                          "id": 1234567890,
                          "properties": {"nickname": "웹소소"},
                          "kakao_account": {"email": "websoso@websoso.org"}
                        }
                        """, MediaType.APPLICATION_JSON));

        KakaoUserInfo userInfo = kakaoClient.getUserInfo(ACCESS_TOKEN);

        assertThat(userInfo.id()).isEqualTo(1234567890L);
        assertThat(userInfo.nickname()).isEqualTo("웹소소");
        assertThat(userInfo.email()).isEqualTo("websoso@websoso.org");
        mockServer.verify();
    }

    @DisplayName("사용자 정보 조회가 4xx로 실패하면 유효하지 않은 access token 예외가 발생한다")
    @Test
    void getUserInfo_clientError() {
        mockServer.expect(requestTo(USER_INFO_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> kakaoClient.getUserInfo(ACCESS_TOKEN))
                .isInstanceOf(CustomKakaoException.class)
                .extracting(throwable -> ((CustomKakaoException) throwable).getICustomError())
                .isEqualTo(INVALID_KAKAO_ACCESS_TOKEN);
        mockServer.verify();
    }

    @DisplayName("사용자 정보 조회가 5xx로 실패하면 카카오 서버 오류 예외가 발생한다")
    @Test
    void getUserInfo_serverError() {
        mockServer.expect(requestTo(USER_INFO_URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> kakaoClient.getUserInfo(ACCESS_TOKEN))
                .isInstanceOf(CustomKakaoException.class)
                .extracting(throwable -> ((CustomKakaoException) throwable).getICustomError())
                .isEqualTo(KAKAO_SERVER_ERROR);
        mockServer.verify();
    }

    @DisplayName("로그아웃 요청은 관리자 키 헤더와 form body를 담아 전송한다")
    @Test
    void logout_success() {
        mockServer.expect(requestTo(LOGOUT_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "KakaoAK " + ADMIN_KEY))
                .andExpect(content().string(EXPECTED_FORM_BODY))
                .andRespond(withSuccess());

        kakaoClient.logout(PROVIDER_USER_ID);

        mockServer.verify();
    }

    @DisplayName("로그아웃 요청이 4xx로 실패하면 외부 요청 실패 예외가 발생한다")
    @Test
    void logout_clientError() {
        mockServer.expect(requestTo(LOGOUT_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> kakaoClient.logout(PROVIDER_USER_ID))
                .isInstanceOf(CustomKakaoException.class)
                .hasMessage("kakao request failed during logout")
                .extracting(throwable -> ((CustomKakaoException) throwable).getICustomError())
                .isEqualTo(KAKAO_REQUEST_FAILED);
        mockServer.verify();
    }

    @DisplayName("로그아웃 요청이 5xx로 실패하면 카카오 서버 오류 예외가 발생한다")
    @Test
    void logout_serverError() {
        mockServer.expect(requestTo(LOGOUT_URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> kakaoClient.logout(PROVIDER_USER_ID))
                .isInstanceOf(CustomKakaoException.class)
                .hasMessage("kakao server error during logout")
                .extracting(throwable -> ((CustomKakaoException) throwable).getICustomError())
                .isEqualTo(KAKAO_SERVER_ERROR);
        mockServer.verify();
    }

    @DisplayName("연결 해제 요청은 관리자 키 헤더와 form body를 담아 전송한다")
    @Test
    void unlink_success() {
        mockServer.expect(requestTo(UNLINK_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "KakaoAK " + ADMIN_KEY))
                .andExpect(content().string(EXPECTED_FORM_BODY))
                .andRespond(withSuccess());

        kakaoClient.unlink(PROVIDER_USER_ID);

        mockServer.verify();
    }

    @DisplayName("연결 해제 요청이 4xx로 실패하면 외부 요청 실패 예외가 발생한다")
    @Test
    void unlink_clientError() {
        mockServer.expect(requestTo(UNLINK_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> kakaoClient.unlink(PROVIDER_USER_ID))
                .isInstanceOf(CustomKakaoException.class)
                .hasMessage("kakao request failed during unlink")
                .extracting(throwable -> ((CustomKakaoException) throwable).getICustomError())
                .isEqualTo(KAKAO_REQUEST_FAILED);
        mockServer.verify();
    }

    @DisplayName("연결 해제 요청이 5xx로 실패하면 카카오 서버 오류 예외가 발생한다")
    @Test
    void unlink_serverError() {
        mockServer.expect(requestTo(UNLINK_URL))
                .andRespond(withServerError());

        assertThatThrownBy(() -> kakaoClient.unlink(PROVIDER_USER_ID))
                .isInstanceOf(CustomKakaoException.class)
                .hasMessage("kakao server error during unlink")
                .extracting(throwable -> ((CustomKakaoException) throwable).getICustomError())
                .isEqualTo(KAKAO_SERVER_ERROR);
        mockServer.verify();
    }
}
