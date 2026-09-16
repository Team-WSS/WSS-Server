package org.websoso.WSSServer.exception.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.websoso.WSSServer.collection.exception.CustomCollectionError.COLLECTION_NOT_FOUND;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.websoso.WSSServer.collection.exception.CustomCollectionException;

/**
 * 공통 오류 응답 계약을 실제 요청으로 확인하기 위한 테스트 전용 Controller.
 *
 * <p>404·405·406·415·403·500은 특정 도메인 API의 계약이 아니라 모든 엔드포인트에 똑같이 적용되는 동작이다.
 * 도메인 Controller로 재현하면 그 API의 계약처럼 보이고 도메인 로직 변경에 따라 테스트가 깨지므로,
 * 오류를 만들 최소한의 엔드포인트만 가진 Controller를 따로 둔다. 문서화 대상이 아니다.
 */
@RestController
@RequestMapping("/test/common-errors")
public class CommonErrorTestController {

    @GetMapping(value = "/json-only", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<Void> jsonOnly() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/json-body", consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<Void> jsonBody() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Void> adminOnly() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/positive/{id}")
    ResponseEntity<Void> positiveId(@PathVariable("id") @Positive Long id) {
        return ResponseEntity.noContent().build();
    }

    /**
     * 인증 헤더가 아닌 필수 헤더를 요구하는 엔드포인트.
     * 인증을 통과한 요청에서 헤더만 빠졌을 때의 응답을 확인하기 위해 둔다.
     */
    @GetMapping("/required-header")
    ResponseEntity<Void> requiredHeader(@RequestHeader(REQUIRED_HEADER_NAME) String requiredHeader) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/domain-error")
    ResponseEntity<Void> domainError() {
        throw new CustomCollectionException(COLLECTION_NOT_FOUND, "collection with the given id was not found");
    }

    /**
     * 예상하지 못한 서버 오류. 메시지에 내부 구조가 섞인 예외를 던져
     * 응답에 원본 내용이 새지 않는지 확인할 수 있게 한다.
     */
    @GetMapping("/unexpected")
    ResponseEntity<Void> unexpected() {
        throw new IllegalStateException(UNEXPECTED_CAUSE_MESSAGE);
    }

    static final String REQUIRED_HEADER_NAME = "X-Required-Header";

    static final String UNEXPECTED_CAUSE_MESSAGE =
            "could not execute statement [insert into user (nickname) values (?)] UNIQUE_NICKNAME_CONSTRAINT";
}
