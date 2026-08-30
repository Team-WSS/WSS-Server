# 에러 코드 (서비스 에러 코드)

실패 응답의 본문은 `code`와 `message` 두 필드를 가진 `ErrorResult` 하나로 통일한다.

```json
{
  "code": "COMMON-001",
  "message": "컬렉션 이름은 비어 있거나, 공백일 수 없습니다."
}
```

- `code`는 클라이언트가 분기 조건으로 쓰는 값이다. 사람이 읽는 문구가 아니라 계약이므로 한 번 정한 값은 바꾸지 않는다.
- `message`는 사람이 읽는 값이다. 클라이언트가 이 값으로 분기하지 않아도 되도록 원인은 항상 `code`로 구분할 수 있어야 한다.
- HTTP 상태만으로는 원인을 구분할 수 없다. 400 하나에 여러 원인이 모이므로 `code`가 실제 구분자다.

## 코드 체계

코드는 `{영역}-{일련번호 3자리}` 형식이다. 영역은 코드가 속한 `ICustomError` 구현 enum 하나와 1:1로 대응한다.

| 영역 | 정의 | 예 |
| --- | --- | --- |
| 도메인 | `CustomCollectionError`, `CustomUserError`처럼 도메인별 enum | `COLLECTION-002`, `USER-006` |
| 공통 | `org.websoso.common.exception.CustomCommonError` | `COMMON-001` |

- 일련번호는 영역 안에서 정의한 순서대로 부여하고 재사용하지 않는다. 쓰지 않게 된 코드의 번호도 비워 둔다.
- 새 코드는 enum의 마지막에 추가한다. 중간에 끼워 넣으면 이미 배포된 코드의 의미가 바뀔 수 있다.
- 상태 코드는 코드마다 enum 정의에 함께 둔다. 같은 코드가 엔드포인트마다 다른 상태로 나가지 않는다.

### 영역 문자열의 실제 표기

영역 문자열은 하나의 표기법으로 통일돼 있지 않다. 이미 배포된 코드가 아래처럼 여러 형태를 쓴다.

| 표기 | 실제 코드 |
| --- | --- |
| 대문자 한 단어 | `NOVEL-001`, `USER-006`, `COMMON-001` |
| 대문자 밑줄 | `USER_NOVEL-001`, `ATTRACTIVE_POINT-001`, `KEYWORD_CATEGORY-001` |
| 대문자 하이픈 | `NOTIFICATION-TYPE-001`, `MINIMUM-VERSION-001` |
| 첫 글자만 대문자 | `Filtering-001` |

**코드는 클라이언트와의 계약이므로 이미 나간 표기를 통일하려고 바꾸지 않는다.** 이 문서가 정의하는 것은
`{영역}-{일련번호 3자리}` 구조뿐이고, 영역 문자열의 표기는 영역마다 이미 정해져 있다.
새 영역을 만들 때만 대문자와 밑줄(`USER_NOVEL`)을 쓴다. 하이픈은 영역과 일련번호를 나누는 구분자와 겹쳐
`NOTIFICATION-TYPE-001`처럼 어디까지가 영역인지 문자열만으로는 읽히지 않는다.
기존 영역에 코드를 추가할 때는 그 영역이 이미 쓰는 표기를 그대로 따른다.

## 공통 코드와 도메인 코드의 구분

**공통 코드(`COMMON-`)는 원인이 도메인과 무관한 요청 처리 오류에만 쓴다.** 어떤 엔드포인트에서 발생하든
같은 원인이면 클라이언트가 똑같이 대응할 수 있는 오류다. 이런 오류는 `GlobalExceptionHandler`가 응답을 만든다.

**도메인 비즈니스 규칙 위반은 공통 코드에 넣지 않는다.** 원인과 클라이언트의 대응이 도메인마다 다르기 때문이다.
컬렉션의 포함 작품 수 제한처럼 도메인이 정의한 규칙은 도메인 enum에 코드를 두고 `AbstractCustomException`으로 던진다.

판단이 애매하면 다음을 확인한다.

- 이 오류를 다른 도메인의 API에서도 똑같은 의미로 내려줄 수 있는가 → 공통 코드
- 이 오류를 설명하려면 도메인 개념을 알아야 하는가 → 도메인 코드

같은 400이라도 요청 형식이 잘못된 것(`COMMON-001`)과 도메인 규칙을 위반한 것(`COLLECTION-002`)은 서로 다른 코드다.
클라이언트가 전자는 입력값을 고쳐 재시도하고 후자는 화면 흐름을 바꿔야 하므로 대응이 다르다.

## 공통 코드 목록

`CustomCommonError`에 정의한다.

| 코드 | HTTP 상태 | 발생 조건 | 응답 message |
| --- | --- | --- | --- |
| `COMMON-001` | 400 Bad Request | `@Valid @RequestBody` DTO의 Bean Validation 실패 | 실패한 검증 애너테이션의 메시지 |
| `COMMON-002` | 400 Bad Request | 요청 본문을 JSON으로 읽지 못함 | `잘못된 JSON 형식입니다.` |
| `COMMON-003` | 400 Bad Request | PathVariable·RequestParam의 Bean Validation 제약 위반 | 실패한 검증 애너테이션의 메시지 |
| `COMMON-004` | 400 Bad Request | 필수 RequestParam 누락 | `필수 요청 파라미터가 없습니다: {이름}` |
| `COMMON-005` | 400 Bad Request | PathVariable·RequestParam의 타입 또는 Enum 변환 실패 | `요청 값의 형식이 올바르지 않습니다: {이름}` |
| `COMMON-006` | 400 Bad Request | 필수 multipart RequestPart 누락 | `필수 요청 파트가 없습니다: {이름}` |
| `COMMON-007` | 400 Bad Request | 업로드 파일 또는 전체 요청 용량 초과 | 파일당·총 용량 제한 안내 |
| `COMMON-008` | 404 Not Found | 매핑된 API 엔드포인트 없음 | `요청한 API 엔드포인트를 찾을 수 없습니다.` |
| `COMMON-009` | 405 Method Not Allowed | 엔드포인트가 지원하지 않는 HTTP 메서드 | `지원하지 않는 HTTP 메서드입니다.` |
| `COMMON-010` | 415 Unsupported Media Type | 지원하지 않는 Content-Type | `지원하지 않는 Content-Type입니다.` |
| `COMMON-011` | 406 Not Acceptable | 지원하지 않는 Accept 타입 | `지원하지 않는 Accept 타입입니다.` |
| `COMMON-012` | 403 Forbidden | Spring Security의 일반 인가 실패 | `요청한 리소스에 접근할 권한이 없습니다.` |
| `COMMON-013` | 409 Conflict | 도메인 코드로 분류되지 않은 DB 무결성 위반 | `DB 무결성 제약조건이 위반되었습니다.` |
| `COMMON-014` | 400 Bad Request | 필수 RequestHeader 누락 | `필수 요청 헤더가 없습니다: {이름}` |
| `COMMON-999` | 500 Internal Server Error | 별도로 처리되지 않은 예상하지 못한 서버 오류 | `서버 내부 오류가 발생했습니다.` |

- DTO 필드마다 별도의 코드를 만들지 않는다. 필드가 늘어날 때마다 코드가 늘면 클라이언트가 분기할 수 없다.
  어느 필드가 실패했는지는 `message`로 전달한다.
- 여러 필드가 함께 실패해도 `message`는 첫 번째 실패 하나만 담는다. `ErrorResult`는 필드별 오류 배열을 갖지 않는다.
- `COMMON-001`과 `COMMON-003`은 둘 다 Bean Validation 실패지만 클라이언트가 고쳐야 하는 위치가 다르다.
  전자는 요청 본문의 필드이고 후자는 경로·쿼리 파라미터다.
- `COMMON-003`은 `@Validated`가 붙은 Controller의 `ConstraintViolationException`과, 붙지 않은 Controller의
  `HandlerMethodValidationException`을 함께 담는다. 서버의 검증 방식은 클라이언트가 알 필요가 없다.
- `COMMON-008`은 API 엔드포인트가 없다는 뜻이고, `NOVEL-001` 같은 도메인 404는 엔드포인트는 있지만
  요청한 자원이 없다는 뜻이다. 전자는 클라이언트가 요청 경로를 고쳐야 하고 후자는 그렇지 않다.
- `COMMON-012`는 도메인 개념 없이 판단하는 일반 인가 실패다. 컬렉션 소유권 위반(`COLLECTION-005`)처럼
  도메인이 판단하는 인가 실패는 도메인 코드를 그대로 쓴다.
- `COMMON-004`와 `COMMON-014`는 둘 다 필수 요청 값 누락이지만 빠진 위치가 쿼리 파라미터인지 헤더인지가 다르다.
  전용 매핑이 없으면 헤더 누락은 fallback으로 넘어가 500 `COMMON-999`가 된다. 클라이언트가 요청만 고치면 되는
  400을 서버 오류로 알려주게 되므로 헤더 누락도 명시적으로 처리한다.
- 인증 헤더가 없는 요청은 `COMMON-014`가 아니다. 인증이 필요한 엔드포인트는 Controller에 도달하기 전에
  `JwtAuthenticationFilter`가 401 `AUTH-001`로 끝낸다. `COMMON-014`는 인증을 통과한 뒤 그 엔드포인트가
  따로 요구하는 헤더가 없을 때의 코드다.

### 404·405·406·415·500이 401이 되지 않아야 하는 이유

프레임워크가 던지는 요청·프로토콜 예외를 `GlobalExceptionHandler`가 잡지 않으면 DispatcherServlet이
컨테이너의 ERROR 디스패치로 넘긴다. ERROR 디스패치는 `/error`에 대한 **새 요청**이고 그 요청은
SecurityContext가 비어 있어 인증에 실패한다. 그 결과 원래 400·404·405·406·415·500이어야 할 응답이
401 `AUTH-001`로 바뀌어 클라이언트가 원인을 알 수 없게 된다.

`/error`를 `permitAll`로 여는 대신 각 예외를 명시적으로 처리해 응답을 확정한다.
Security 인가 정책을 오류 처리 편의를 위해 바꾸지 않는다.

## 메시지 규칙

코드에 따라 `message`를 정하는 방식이 두 가지다.

- **정의된 메시지를 그대로 쓴다.** 원인이 코드 하나로 완전히 설명되는 오류다. `description`이 곧 응답 메시지다.
  대부분의 도메인 코드와 `COMMON-002`가 여기에 해당한다.
- **코드는 고정하고 메시지만 조합한다.** 원인은 같지만 구체적인 내용이 요청마다 달라지는 오류다. `COMMON-001`이 여기에 해당한다.
  `GlobalExceptionHandler`의 `errorResponse(ICustomError, String)`으로 정의의 상태 코드·코드에 동적 메시지를 붙인다.
  이때 `description`은 구체적인 원인을 알 수 없을 때 쓰는 기본 메시지다.

동적 메시지를 쓰더라도 코드는 반드시 `ICustomError` 정의에서 가져온다. 핸들러에 코드 문자열을 직접 적지 않는다.

## HTTP 상태 이름을 `code`로 쓰지 않는다

`BAD_REQUEST`, `CONFLICT` 같은 HTTP 상태 이름은 서비스 에러 코드가 아니다. 상태 하나에 원인이 여러 개 모이므로
클라이언트가 그 값으로 원인을 구분할 수 없고, 코드와 상태가 같은 값이면 코드가 있을 이유도 없다.

응답을 만드는 모든 경로는 `ICustomError` 정의에서 `code`를 가져온다. 핸들러가 코드 문자열을 직접 적지 않고,
`ErrorResponseDocumentation.errorLine`도 코드를 문자열로 받는 입구를 두지 않는다.

## 오류 응답에 담지 않는 것

- 원본 예외 메시지와 스택 트레이스. `COMMON-999`는 정의된 고정 문자열만 응답하고 전체 예외는 로그에만 남긴다.
- SQL과 DB 제약조건 이름. `DataIntegrityViolationException`의 root cause는 도메인 코드로 분류할지 판단하는 데만 쓰고
  응답에는 내보내지 않는다.
- 변환에 실패한 값 자체. `COMMON-005`는 파라미터 이름만 알려준다.

검증 애너테이션에 우리가 직접 쓴 메시지는 예외다. 클라이언트가 입력을 고치는 데 필요하고 내부 구조를 드러내지 않으므로
`COMMON-001`, `COMMON-003`은 그 메시지를 그대로 내려준다.

## 코드를 추가할 때

1. 도메인 규칙이면 해당 도메인 enum에, 요청 처리 오류면 `CustomCommonError`에 상수를 추가한다.
2. 상수 이름은 원인을 서술한다(`INVALID_REQUEST_FIELD`). 코드 문자열을 이름에 넣지 않는다.
3. 코드, `description`, 상태 코드를 함께 정의한다. 상태 코드를 호출부에서 정하지 않는다.
4. 도메인 코드는 해당 예외를 던지는 곳에 테스트를 추가한다. 공통 코드는 어떤 예외가 어떤 코드가 되는지를
   `GlobalExceptionHandlerTest`에서, 실제 요청이 Security 필터를 지나 그 코드로 응답하는지를
   `CommonErrorResponseTest`에서 확인한다. 404·405·406·415·403·500은 필터 체인 없이 검증할 수 없다.
5. 그 코드를 응답하는 API의 문서 테스트를 갱신한다. 규칙은 [api-docs.md](api-docs.md)의 "오류 응답 문서화 규칙"을 따른다.
   같은 상태 코드에 코드가 여러 개면 문서 식별자를 나눠 named example로 구분한다.
6. 클라이언트가 이미 쓰고 있는 코드는 바꾸지 않는다. 바꿔야 한다면 별도 이슈로 호환성 처리를 함께 정한다.
