package org.websoso.WSSServer.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.user-info-url}")
    private String kakaoUserInfoUrl;

}
