# API 문서 (Spring REST Docs → OpenAPI 3)

API 명세는 Controller 문서 테스트(MockMvc)의 실제 요청·응답으로 생성한다.
런타임에 코드를 분석해 명세를 만드는 라이브러리는 사용하지 않으며, 명세의 기준은 REST Docs 산출물 하나다.

## 구성

| 항목 | 경로 |
| --- | --- |
| REST Docs snippet | `build/generated-snippets` |
| OpenAPI 3 명세 | `build/api-spec/openapi3.json` |
| Swagger UI 제공용 리소스 | `build/api-docs-resources/swagger-ui/openapi3.json` |
| 실행 중 명세 URL | `/swagger-ui/openapi3.json` |
| Swagger UI | `/swagger-ui.html` → `/swagger-ui/index.html` |

- 문서 테스트는 클래스 이름을 `*DocsTest`로 만든다. `restDocsTest` task가 이 이름으로 문서 테스트만 실행한다.
- 문서 테스트는 `@AuthenticatedControllerTest` 기반의 MockMvc 슬라이스로 작성한다. 외부 DB, Redis, 소셜 API 없이 실행된다.
- 요청은 `RestDocumentationRequestBuilders`로 만들어야 URL 템플릿이 명세에 기록된다.
- 생성 명세의 `servers[].url`은 `/`다. Swagger UI를 연 origin을 그대로 사용한다.

## 문서화 대상의 책임 경계

문서 테스트는 **Controller와 애플리케이션 로직이 정의한 계약**만 문서화한다. 대상은 두 가지다.

- 성공 응답
- 애플리케이션 예외로 정의된 실패 응답
  - Controller가 호출하는 Application·Service·Client가 던지는 `AbstractCustomException`
  - `JwtAuthenticationFilter`가 직접 쓰는 인증 오류(`CustomAuthError`)
  - `GlobalExceptionHandler`가 응답 본문을 만들어 주는 오류(예: 본문이 JSON으로 읽히지 않을 때의 `400`)

Controller에 들어오기 전에 프레임워크가 만드는 응답은 문서화하지 않는다.
이 API 고유의 계약이 아니라 모든 엔드포인트에 똑같이 적용되는 동작이라, 명세에 넣어도 클라이언트가 이 API에 대해 알 수 있는 것이 없다.
특히 다른 HTTP 메서드로 요청해 `405`를 문서화하면 명세에 그 메서드의 엔드포인트가 통째로 생겨 버린다.

## 오류 응답 문서화 규칙

명세의 기준은 문서 테스트가 실제로 받은 응답이다. 성공 응답만 문서화하면 클라이언트는 실패를 명세에서 알 수 없다.
API를 새로 문서화할 때 아래를 지킨다.

1. **애플리케이션 예외로 정의된 실패 응답을 전부 요청으로 재현한다.**
   위 책임 경계의 경로를 따라가 상태 코드를 목록화한 뒤 각 응답을 실제 요청으로 재현한다.
   재현하지 못한 응답은 문서에 쓰지 않는다.
2. **문서 테스트가 응답 형식까지 검증한다.**
   상태 코드만 보지 말고 `jsonPath("$.code")`, `jsonPath("$.message")`로 서비스 에러 코드와 메시지를 확인한다.
   본문이 없는 성공 응답은 `content().string("")`으로 없다는 사실을 확인한다.
3. **상태 코드와 메시지는 `ErrorCode` 정의에서 가져온다.**
   `ACCESS_TOKEN_EXPIRED.getCode()`, `ACCESS_TOKEN_EXPIRED.getDescription()`처럼 enum에서 읽어 단언한다.
   문자열을 테스트에 옮겨 적으면 오탈자나 정의 변경을 테스트가 잡지 못한다.
   `GlobalExceptionHandler`가 직접 만드는 메시지처럼 enum이 없는 경우에만 상수로 두고, 실제 응답과 일치하는지 같은 테스트에서 확인한다.
4. **오류 본문은 공통 `ErrorResult` 스키마를 재사용한다.**
   `support/docs/ErrorResponseDocumentation`의 `ERROR_RESULT_SCHEMA`와 `errorResultFields()`를 쓴다.
   생성 명세에서 오류 응답들이 `components.schemas.ErrorResult` 하나를 공유한다.
   공유 스키마이므로 필드 서술에 특정 오류의 코드나 메시지를 적지 않는다. 적으면 마지막에 처리된 문서의 서술만 남는다.

### 같은 상태 코드에 서비스 에러 코드가 여러 개일 때

`POST /auth/logout`의 401처럼 한 상태 코드에 `AUTH-000`(만료 Access Token), `AUTH-001`(유효하지 않은 토큰),
`AUTH-003`(잘못된 토큰 유형)이 함께 있는 경우가 있다. 상태 코드 하나로는 원인을 구분할 수 없으므로 두 가지를 함께 한다.

- **코드마다 문서 식별자를 나눈다.** `document("auth-logout-invalid-token", ...)`처럼 원인을 담은 이름을 쓴다.
  restdocs-api-spec은 같은 경로·메서드·상태 코드의 응답들을 이 식별자 이름의 **named example**로 묶는다.
  Swagger UI의 응답 예시 선택 상자에서 코드별 본문을 각각 고를 수 있다.
- **엔드포인트 `description`에 전체 목록을 적는다.** `ErrorResponseDocumentation.errorLine`으로
  `- 401 Unauthorized · AUTH-000 — 메시지` 형태의 Markdown 목록을 만든다.
  서술에는 인라인 코드(백틱)를 쓰지 않는다. Swagger UI가 백틱을 monospace 배지로 강조해
  상태 코드와 에러 코드가 줄마다 반복되면 목록 전체가 읽기 어려워진다.
  named example 식별자도 적지 않는다. 각 응답의 Examples 선택 상자에 이미 보인다.
  이때 **같은 경로·메서드의 모든 문서 식별자가 똑같은 `description`을 쓴다.**
  생성기가 여러 문서 중 하나의 `summary`/`description`만 골라 쓰기 때문에, 어느 것이 뽑혀도 목록이 온전해야 한다.
- **요청 본문은 성공 문서 하나만 남긴다.** restdocs-api-spec은 같은 경로·메서드의 모든 문서에서 요청 본문을 모아
  문서 식별자를 키로 한 `requestBody` example 목록을 만든다. 오류 문서까지 그대로 두면 Swagger UI의
  Request Body 예시 선택 상자가 오류 시나리오 이름으로 채워지고, 어느 것을 골라도 같은 본문이라 고를 이유가 없다.
  오류 문서는 `document("auth-logout-invalid-token", withoutRequestBody(), resource(...))`처럼
  `support/docs/RequestPreprocessors.withoutRequestBody()`를 적용해 실제 요청은 그대로 보내고 명세에서만 본문을 뺀다.

### 인증이 필요한 엔드포인트

`Authorization` 헤더는 `requestHeaders`로 선언하지 않는다. 실제 요청 헤더로만 보낸다.

- restdocs-api-spec은 요청의 `Bearer` 토큰을 보고 `components.securitySchemes.bearerAuthJWT`와
  operation의 security requirement를 스스로 만든다. Swagger UI는 이것으로 상단 **Authorize** 입력을 띄운다.
- 헤더를 `requestHeaders`로도 선언하면 같은 헤더가 Authorize와 요청 파라미터로 두 번 나오고,
  문서 테스트가 발급한 토큰이 파라미터 `example` 값으로 명세에 그대로 박힌다.
- Swagger는 토큰을 발급하지 않는다. 엔드포인트 `description`에 Authorize에 실제 Access Token을 넣으라는
  한 줄 안내를 둔다.

### 개발 중 Access Token을 얻는 흐름

소셜 로그인만으로는 로컬에서 Access Token을 얻기 어렵다. Deprecated `POST /users/login`을
개발 보조용으로 함께 문서화해 두었으므로 Swagger UI에서 다음 순서로 인증 API를 호출한다.

1. `POST /users/login`을 Try it out으로 호출한다. 요청 본문은 로컬 DB에 실제로 있는 사용자 ID 숫자다.
2. 200 응답의 `Authorization` 값을 복사한다. Access Token만 발급되고 Refresh Token은 발급되지 않는다.
3. 상단 **Authorize**에 그 값을 넣는다. 이후 `bearerAuthJWT`를 요구하는 API를 그대로 호출할 수 있다.

- 운영 클라이언트용 API가 아니라는 것과 위 사용 순서는 엔드포인트 `description`에 적는다.
- 개발 보조 API임이 Swagger UI에서 드러나도록 `ResourceSnippetParameters.builder().deprecated(true)`를 쓴다.
  생성기는 **같은 경로·메서드의 모든 문서**가 `deprecated(true)`일 때만 operation에 `deprecated: true`를 남기므로,
  오류 응답 문서에도 똑같이 지정한다.
- 이 API는 인증이 필요 없으므로 문서 테스트 요청에 `Authorization` 헤더를 넣지 않는다.
  넣으면 인증이 필요 없는 API에 security requirement가 붙는다.
- 응답 예시의 Access Token은 문서용 placeholder를 쓴다. 실제 토큰이나 비밀값을 산출물에 남기지 않는다.

#### 알려진 제약: 본문이 JSON 객체가 아닌 요청의 스키마

`POST /users/login`의 실제 wire body는 JSON 객체가 아니라 숫자 하나(`42`)다.
그런데 생성 명세의 `requestBody` 스키마는 `{"type": "object"}`이고,
이름을 주지 않아 `components.schemas`에 `users-login<hash>` 형태의 자동 생성 이름으로 올라간다.
**이것은 요청 계약을 정확히 표현하지 못하는 생성기 제약이며, 문서 테스트 쪽에서 고칠 수 없다.**

`JsonSchemaFromFieldDescriptorsGenerator.generateSchema`가 루트를 항상 `ObjectSchema.builder()`로 만들고,
`unWrapRootArray`가 루트 `[]` 배열만 되돌리기 때문이다. 0.19.4와 최신 0.20.1·master 소스가 모두 같아
버전을 올려도 달라지지 않는다. 루트 스칼라를 서술하려는 시도는 REST Docs 자체 payload 검증에서 막힌다.

| 시도 | 결과 |
| --- | --- |
| `fieldWithPath("[]").type(NUMBER)` | `ClassCastException: Integer cannot be cast to List` |
| `fieldWithPath("").type(NUMBER)` | `IndexOutOfBoundsException: Index 0 out of bounds for length 0` |
| `.requestSchema(Schema.schema("..."))` 단독 | 해시 이름은 사라지지만 본문은 `{"type": "object"}` 그대로 |

`toMediaType`은 항상 스키마를 만들고 `extractDefinitions`는 조건 없이 `components`로 추출하므로
스키마를 아예 빼는 것도 불가능하다. Gradle 플러그인의 `openapi3` 확장에도 스키마 override 훅이 없다.

생성 명세를 문자열로 고치는 후처리는 하지 않는다. 명세의 기준은 문서 테스트 산출물 하나여야 하고,
후처리를 넣는 순간 생성 결과와 실제 테스트가 어긋나도 알 수 없다.
대신 요청 본문이 숫자 하나라는 사실은 엔드포인트 `description`과 Request Body 예시(`42`)로 전달한다.
Swagger UI의 Try it out은 예시 값으로 채워지므로 호출에는 영향이 없다.

요청 본문을 정확한 스키마로 표현해야 한다면 방법은 요청 계약을 JSON 객체(`{"userId": 42}`)로 바꾸는 것뿐이다.
이는 프로덕션 API 계약 변경이라 별도 이슈와 승인이 필요하다.

### 중첩 응답의 구조를 읽히게 문서화한다

응답이 여러 겹으로 중첩되면(페이지 정보 + 카드 배열 + 카드 안의 객체) 필드 목록만으로는
어떤 값이 무엇에 속하고 어디에 쓰이는지 읽히지 않는다. 아래를 지킨다.

- **엔드포인트 `description`에 응답의 겹 구조를 한 번 설명한다.** 어떤 묶음으로 나뉘는지,
  각 묶음이 화면에서 무엇에 쓰이는지 적는다. 필드 목록은 겹 사이의 관계를 설명하지 못한다.
- **필드 서술에 어느 겹의 값인지 표시한다.** `[페이지 정보]`, `[컬렉션 카드]`처럼 겹 이름을 앞에 붙이면
  Swagger UI의 평면 필드 목록에서도 소속이 보인다.
- **여러 엔드포인트가 같은 중첩 구조를 공유하면 서술에 그 사실을 적는다.**
  클라이언트가 같은 구조를 위치마다 따로 다루지 않게 한다.
- **오해하기 쉬운 값은 아닌 것을 함께 적는다.** "이번 페이지 개수가 아니다"처럼 쓴다.
- **예시는 계약을 이해하는 데 필요한 경계까지 담는다.** 배열에 최대 개수가 있으면 최대 개수로 만들고,
  같은 값이 두 곳에 중복될 수 있으면 중복된 예시를 만든다. 원소 한두 개짜리 예시로는 그 사실이 읽히지 않는다.

#### 알려진 제약: 중첩 객체에는 독립 스키마 이름을 줄 수 없다

`Schema.schema("...")`는 요청·응답 **루트**에만 이름을 붙인다. 생성기는 중첩 객체와 배열 원소를
`components.schemas`로 분리하지 않고 루트 스키마 안에 인라인으로 펼친다.
따라서 여러 엔드포인트가 공유하는 중첩 구조를 명세에서 하나의 스키마로 참조하게 만들 수 없다.

실제 계약에 없는 스키마 이름을 후처리로 만들어 붙이지 않는다. 명세의 기준은 문서 테스트 산출물 하나여야 한다.
구조를 공유한다는 사실은 위 서술 규칙으로 전달하고, 응답 DTO 쪽에서 실제로 같은 타입을 공유해 계약을 맞춘다.

### 계약 검증은 문서 테스트에서만 한다

응답 계약의 검증 책임은 문서 테스트에 있다. Gradle에는 문서 생성 파이프라인만 두고,
생성된 명세를 다시 파싱해 엔드포인트별 상태 집합을 중복 검증하는 빌드 로직은 두지 않는다.
같은 계약을 두 곳에서 관리하면 기대값이 어긋날 때 어느 쪽이 기준인지 알 수 없다.

문서 테스트를 고친 뒤에는 `./gradlew clean apiDocs`로 명세를 다시 만들고,
`build/api-spec/openapi3.json`에서 각 오류의 named example에 `code`와 `message`가 담겼는지 눈으로 확인한다.

## 명령

```bash
./gradlew apiDocs                  # 문서 테스트 실행 후 OpenAPI 3 명세 생성
./gradlew apiDocs bootJar          # 생성한 명세를 포함해 실행 jar 패키징
./gradlew apiDocs bootRun          # 로컬에서 Swagger UI로 생성 명세 확인
./gradlew build -x test            # 컴파일 검증
./gradlew test                     # 전체 테스트
```

CI에서는 `./gradlew clean apiDocs bootJar`로 명세 생성과 패키징을 함께 수행한다.

## Swagger UI 제공 방식

- Swagger UI는 `org.webjars:swagger-ui` WebJar 번들을 애플리케이션이 직접 제공한다. 외부 CDN을 사용하지 않는다.
- `SwaggerUiConfig`가 `/swagger-ui/**`를 `classpath:/swagger-ui/`(생성 명세, 초기화 스크립트)와 WebJar 디렉터리 순서로 매핑하고,
  `/swagger-ui.html`을 `/swagger-ui/index.html`로 리다이렉트한다. 둘 다 기존에 이미 공개된 경로다.
- `src/main/resources/swagger-ui/swagger-initializer.js`가 WebJar 기본 초기화 스크립트를 대체해 `./openapi3.json`을 읽는다.
- 노출 여부는 기존 배포 설정 계약을 유지하기 위해 `springdoc.swagger-ui.enabled`로 제어한다(dev `true`, prod `false`).
  값이 없으면 제공하지 않는다.

## 정책

- `apiDocs`는 snippet과 명세를 지운 뒤 다시 생성한다. 문서 테스트가 실패하면 이전 명세가 남지 않고, 그 상태로 패키징되지도 않는다.
- 문서 산출물은 모두 `build/` 아래에서 생성하며 Git으로 추적하지 않는다. 항상 `./gradlew apiDocs`로 다시 만든다.
- 명세 생성은 전체 테스트(`check`)가 아니라 문서 테스트에만 의존한다. 외부 인프라가 필요한 통합 테스트와 분리한다.
