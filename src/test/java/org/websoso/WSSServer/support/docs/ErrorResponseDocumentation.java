package org.websoso.WSSServer.support.docs;

import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.Schema;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.websoso.common.exception.ErrorResult;
import org.websoso.common.exception.ICustomError;

/**
 * 오류 응답 본문({@link ErrorResult})을 문서화하는 공통 도구.
 * 오류 본문을 내려주는 응답은 전부 {@code code}, {@code message} 두 필드로 같은 모양이므로,
 * 문서 테스트마다 필드 서술을 다시 쓰지 않고 여기서 가져다 쓴다.
 *
 * <pre>
 * .responseSchema(ERROR_RESULT_SCHEMA)
 * .responseFields(errorResultFields())
 * </pre>
 *
 * <p>{@link #ERROR_RESULT_SCHEMA}를 함께 지정하면 생성 OpenAPI의 오류 응답들이
 * {@code components.schemas.ErrorResult} 하나를 공유한다. 공유 스키마이므로 필드 서술은
 * 특정 오류에 종속되지 않은 값으로 고정한다. 오류마다 다른 코드와 메시지는 스키마가 아니라
 * 응답 예시(named example)와 엔드포인트 서술({@link #errorLine})로 구분한다.
 */
public final class ErrorResponseDocumentation {

    public static final Schema ERROR_RESULT_SCHEMA = Schema.schema("ErrorResult");

    private ErrorResponseDocumentation() {
    }

    /**
     * 공통 오류 응답 본문의 필드 서술. 모든 오류 응답이 같은 스키마를 공유하도록 서술을 고정한다.
     */
    public static List<FieldDescriptor> errorResultFields() {
        return List.of(
                fieldWithPath("code").type(STRING).description(
                        "오류 코드. 서비스 에러 코드가 정의된 오류는 그 코드이고, 정의되지 않은 오류는 HTTP 상태 이름이다."),
                fieldWithPath("message").type(STRING).description("오류 메시지"));
    }

    /**
     * 엔드포인트 서술에 넣을 오류 응답 한 줄. Swagger UI는 서술을 Markdown 목록으로 렌더링한다.
     * 같은 상태 코드에 코드가 여러 개일 때 목록에서 각각을 구분할 수 있게 한다.
     *
     * <p>인라인 코드(백틱)를 쓰지 않는다. Swagger UI가 백틱을 monospace 배지로 강조해
     * 상태 코드와 에러 코드가 줄마다 반복되면 목록 전체가 읽기 어려워지기 때문이다.
     */
    public static String errorLine(ICustomError error) {
        return errorLine(error.getStatusCode(), error.getCode(), error.getDescription());
    }

    /**
     * 상태 코드와 오류 코드는 정의에서 가져오고 메시지만 실제 응답 값으로 대체한 오류 한 줄.
     * 공통 오류 코드처럼 응답 메시지가 요청마다 달라지는 오류를 서술할 때 쓴다.
     */
    public static String errorLine(ICustomError error, String message) {
        return errorLine(error.getStatusCode(), error.getCode(), message);
    }

    public static String errorLine(HttpStatus status, String code, String message) {
        return "- %d %s · %s — %s".formatted(status.value(), status.getReasonPhrase(), code, message);
    }
}
