package org.websoso.WSSServer.support.docs;

import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;

/**
 * 문서 테스트가 실제로 보낸 요청과 명세에 남길 요청을 분리하는 REST Docs 전처리기.
 *
 * <pre>
 * .andDo(document("auth-logout-invalid-token", withoutRequestBody(), resource(...)))
 * </pre>
 */
public final class RequestPreprocessors {

    private RequestPreprocessors() {
    }

    /**
     * 요청 본문을 명세에 남기지 않는다. 실제 요청은 그대로 보내고 문서화 직전에만 본문을 비운다.
     *
     * <p>restdocs-api-spec은 같은 경로·메서드의 모든 문서에서 요청 본문을 모아
     * 문서 식별자를 키로 한 requestBody example 목록을 만든다. 오류 응답까지 문서 식별자를 나눠 두면
     * Swagger UI의 Request Body 예시 선택 목록이 오류 시나리오 이름으로 채워지고,
     * 어느 것을 골라도 같은 본문이라 고를 이유가 없다.
     * 요청 본문은 성공 문서 하나만 남기고, 오류마다 다른 응답은 응답 예시로 구분한다.
     */
    public static OperationRequestPreprocessor withoutRequestBody() {
        return request -> new OperationRequestFactory().createFrom(request, new byte[0]);
    }
}
