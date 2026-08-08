# 컬렉션 정책

이 문서는 컬렉션 기능에서 지켜야 하는 도메인 규칙과 각 기능의 처리 흐름, 그리고 신규 테이블과 제약조건을 정리한다.
현재 적용 범위는 컬렉션 생성·수정·삭제(#560)이며, 목록·상세 조회와 접근 정책(#561), 좋아요(#562)는 포함하지 않는다.

## 1. 컬렉션

- 컬렉션은 사용자가 작품을 주제별로 묶은 단위이며 소유자는 컬렉션을 만든 사용자 한 명이다.
- 사용자별 컬렉션 생성 개수는 제한하지 않는다.
- 한 사용자가 동일한 이름의 컬렉션을 여러 개 만들 수 있다. 따라서 이름은 컬렉션 식별자가 될 수 없다.
- 컬렉션 이름은 필수이며 공백을 포함해 20자 이하이다.
- 컬렉션 설명은 선택이며 60자 이하이다.
- 컬렉션은 기본적으로 공개 상태로 생성한다.
- 컬렉션에는 최소 1개, 최대 100개의 작품을 포함할 수 있다.
- 같은 작품을 한 컬렉션에 중복으로 포함할 수 없다.
- 대표 작품은 클라이언트가 지정하며 반드시 해당 컬렉션에 포함된 작품이어야 한다.

도메인 클래스 이름은 `Collection`을 사용한다. `java.util.Collection`과 단순 이름이 겹치지만 컬렉션을 가리키는
도메인 용어를 우선하며, 이 패키지의 코드는 `java.util.Collection`을 사용하지 않고 항상 `List`/`Set` 등 구체 타입을 쓴다.

## 2. 이름과 설명 처리

- 이름은 `null`, 빈 문자열, 공백만 있는 문자열을 모두 거부한다.
- 이름의 앞뒤 공백은 제거하지 않는다. 이름 길이 기준이 "공백을 포함해 20자 이하"이므로 공백도 글자 수에 센다.
- 설명은 `null`, 빈 문자열, 공백만 있는 문자열을 모두 `null`로 정규화해 저장한다.
  조회 응답이 "설명 없음"을 한 가지 값으로만 표현하도록 하기 위함이다.
- 수정 요청에서 설명을 `null`이나 빈 문자열로 보내면 설명 삭제로 해석한다.

## 3. 공개 여부

- 생성 요청에서 공개 여부를 생략하면 서버가 공개(`true`)로 생성한다.
- 수정 요청에서는 공개 여부를 필수로 받는다. 생략을 공개로 해석하면 비공개 컬렉션을 수정할 때 필드를 빠뜨리는
  것만으로 컬렉션이 조용히 공개로 바뀌기 때문이다.

컬렉션은 항상 공개이거나 비공개이며 "값이 없는" 상태가 없다. 반면 요청 DTO는 값이 생략됐는지를 알아야 한다.
그래서 요청 DTO와 엔티티가 서로 다른 타입을 쓴다.

| 위치 | 타입 | 이유 |
|---|---|---|
| `CollectionCreateRequest.isPublic` | `Boolean` | 생략(`null`)과 명시적인 `false`를 구분해야 기본값 공개를 적용할 수 있다 |
| `CollectionUpdateRequest.isPublic` | `@NotNull Boolean` | 생략을 조용히 `false`로 처리하지 않고 검증 오류로 돌려보낸다 |
| `Collection.isPublic` | `boolean` | 비어 있을 수 없는 값이므로 원시 타입으로 불변식을 타입에 담는다 |

생성 팩터리만 입력 경계에서 `Boolean`을 받고, 기본값 적용은 엔티티 생성자가 책임진다.
생성자가 `null`을 `DEFAULT_IS_PUBLIC`(`true`)로 정규화한 뒤 원시 타입 필드에 저장하므로,
어떤 경로로 컬렉션을 만들어도 공개 여부는 항상 결정된 값으로 저장된다. 명시적인 `false`는 그대로 `false`다.
수정은 이미 `@NotNull`로 검증된 값만 받으므로 `boolean`을 그대로 받는다.
엔티티가 원시 타입이므로 접근자 이름은 `getIsPublic()`이 아니라 `isPublic()`이다.

기본값은 DB `DEFAULT`로 선언하지 않는다. `is_public`은 `NOT NULL`이고 애플리케이션이 항상 값을 채워 넣으므로
DB 기본값은 실제로 쓰이지 않으면서 "기본 공개"라는 정책을 애플리케이션과 DB 두 곳에 중복으로 두게 된다.
정책이 바뀌면 코드와 DDL을 함께 고쳐야 하고, DB 종속적인 `columnDefinition` 문자열이 엔티티에 남는다.

## 4. 컬렉션 생성

`POST /collections`를 사용하며 로그인한 사용자만 요청할 수 있다.

1. 작품 수가 1개 이상 100개 이하인지 확인한다.
2. 같은 작품이 중복으로 포함되지 않았는지 확인한다.
3. 대표 작품이 포함 작품 중 하나인지 확인한다.
4. 요청한 작품이 모두 존재하는지 한 번의 조회로 확인한다.
5. 컬렉션과 컬렉션 작품을 저장한다.

- 성공하면 `201 Created`와 생성된 `collectionId`를 반환한다.
  같은 사용자가 같은 이름의 컬렉션을 여러 개 만들 수 있어 클라이언트가 이름으로 방금 만든 컬렉션을 찾을 수 없으므로,
  생성 직후 상세 조회와 공유를 위해 식별자를 반환한다.
- 요청한 작품은 요청 배열 순서대로 저장한다. 같은 요청으로 추가된 작품끼리도 식별자 순서가 결정적으로 정해진다.

## 5. 컬렉션 수정

`PUT /collections/{collectionId}`를 사용하며 소유자만 요청할 수 있다. 성공하면 `204 No Content`를 반환한다.

1. 컬렉션이 존재하는지 확인한다.
2. 요청한 사용자가 소유자인지 확인한다.
3. 작품 수, 중복 작품, 대표 작품 포함 여부를 확인한다.
4. 요청한 작품이 모두 존재하는지 한 번의 조회로 확인한다.
5. 이름, 설명, 공개 여부, 대표 작품을 갱신하고 포함 작품을 delta로 갱신한다.

### 5.1 포함 작품은 delta로만 갱신한다

수정 요청은 전체 교체 의미로 받지만 내부적으로는 전체 삭제 후 재삽입을 하지 않는다.

- 제거된 작품만 삭제한다.
- 새로 추가된 작품만 저장한다.
- 계속 포함되는 작품의 `collection_novel` 행은 손대지 않는다.

이유는 두 가지다.

1. `collection_novel.created_date`는 "작품이 컬렉션에 추가된 시점"을 뜻하며 #561의 최근 추가 작품 미리보기와
   추가 시점 기준 정렬의 기준값이다. 전체 삭제 후 재삽입하면 컬렉션을 수정할 때마다 모든 작품의 추가 시점이
   수정 시각으로 리셋되어 정렬 기준이 무의미해지고, 과거 값을 복구할 수 없다.
2. 같은 트랜잭션에서 같은 `(collection_id, novel_id)`를 삭제하고 다시 저장하면 유니크 제약 위반이 발생한다.
   Hibernate는 저장을 삭제보다 먼저 반영하기 때문이다. delta 갱신은 애초에 재삽입을 하지 않아 이 문제가 없다.

컬렉션 자체의 `modified_date`는 수정 시각으로 갱신되므로 언제 수정했는지는 그대로 남는다.
`collection.created_date`는 바뀌지 않으므로 #561의 "최초 생성 시점 기준" 컬렉션 정렬도 영향을 받지 않는다.

### 5.2 수정 시각은 명시적으로 갱신한다

검증을 통과한 수정은 이름·설명·공개 여부·대표 작품이 이전과 같더라도 항상 `collection.modified_date`를 갱신한다.

포함 작품만 바뀌면 `collection` 행 자체는 변경되지 않아 자동 갱신되는 `modified_date`가 그대로 남는다.
따라서 도메인이 수정 끝에 명시적으로 수정 시각을 표시한다.
서재 평가에서 자식만 바뀐 경우 `UserNovel.touch()`를 호출하는 기존 방식과 같다.

정책 위반으로 실패한 수정은 아무것도 바꾸지 않으므로 수정 시각도 갱신하지 않는다.

### 5.3 동시 수정은 컬렉션 단위로 직렬화한다

수정과 삭제는 대상 컬렉션 행에 쓰기 잠금을 걸고 시작한다.
잠금이 없으면 같은 컬렉션에 대한 동시 수정 요청이 각자 읽은 포함 작품을 기준으로 delta를 계산해
서로의 변경을 덮어쓸 수 있다.

- 잠그는 대상은 `collection` 루트 행 하나뿐이다.
- 포함 작품까지 조인해 잠그지 않는다. 여러 컬렉션이 공유하는 `novel` 행을 잠그게 되어
  서로 무관한 컬렉션 수정끼리 대기하기 때문이다.
- 조회 전용 경로는 잠금 조회를 사용하지 않는다. #561의 목록·상세 조회는 잠금 없는 조회를 그대로 쓴다.

- 포함 작품이 모두 제거되는 수정은 허용하지 않는다. 빈 컬렉션이 필요하면 컬렉션 자체를 삭제한다.
- 수정으로 대표 작품이 제거되는 경우 클라이언트가 변경된 대표 작품을 함께 전달해야 한다.

## 6. 컬렉션 삭제

`DELETE /collections/{collectionId}`를 사용하며 소유자만 요청할 수 있다. 성공하면 `204 No Content`를 반환한다.

- 컬렉션 작품은 컬렉션과 같은 애그리거트이므로 `cascade`와 `orphanRemoval`로 함께 삭제된다.
- 컬렉션 좋아요(#562)는 별도 애그리거트이므로 컬렉션 엔티티에 매핑하지 않는다.
  좋아요를 구현할 때 `CollectionManagementApplication.delete`에 좋아요 정리를 명시적으로 추가한다.
  피드 삭제가 댓글·좋아요를 Application에서 순서대로 지우는 기존 방식과 같다.
- 삭제는 hard delete이며 soft delete를 사용하지 않는다.
- 삭제도 수정과 같은 잠금 조회로 대상 컬렉션을 가져온다.

### 6.1 회원 탈퇴 시 컬렉션 정리

탈퇴한 사용자의 컬렉션은 삭제하지 않고 보존한다. 피드·댓글 작성자 익명화와 같은 방식으로
소유자만 알 수 없는 사용자(`user_id = -1`)로 넘긴다. 컬렉션은 다른 사용자가 이미 보고 있을 수 있는
공개 콘텐츠이므로, 작성자가 탈퇴했다는 이유만으로 내용까지 사라지지 않게 한다.

- 컬렉션은 소유자가 반드시 있어야 하므로(`collection.user_id`가 NOT NULL) 사용자를 지우기 전에 소유자를 옮긴다.
- 알 수 없는 사용자 `-1`이 존재한다는 전제는 기존 피드·댓글 익명화와 같다. 새로 만들지 않는다.
- 참고 DDL의 `fk_collection_user`에는 `ON DELETE CASCADE`를 걸지 않는다.
  정리 책임은 DB가 아니라 애플리케이션에 둔다. 탈퇴 흐름에서 어떤 데이터가 어떻게 처리되는지
  코드에서 드러나야 하고, 기존 탈퇴 정리(리프레시 토큰 삭제, 피드·댓글 작성자 익명화)도 같은 방식이기 때문이다.
- `AccountApplication`의 탈퇴 정리에서 피드·댓글 작성자 익명화와 나란히, 사용자 삭제 전에 소유자를 넘긴다.
- 소유자 이관은 QueryDSL `update`로 `collection.user_id`만 한 번에 갱신한다.
  컬렉션을 엔티티로 불러올 이유가 없고 자식 행(`collection_novel`)은 손대지 않으므로,
  삭제와 달리 벌크 갱신으로 인해 놓치는 cascade 동작이 없다.
- `collection_novel`은 컬렉션을 그대로 따라가므로 별도 정리가 필요 없다.
- 컬렉션 좋아요(#562)를 구현하면 탈퇴한 사용자가 **남긴** 좋아요를 어떻게 처리할지 그때 정한다.
  컬렉션 자체가 보존되므로 컬렉션에 **달린** 좋아요는 탈퇴와 함께 정리할 필요가 없다.

컬렉션 삭제 API(6절)의 hard delete는 그대로 유지한다. 소유자가 직접 지운 컬렉션과
소유자가 탈퇴해 주인이 없어진 컬렉션은 다른 상황이다.

## 7. 소유자 권한

- 컬렉션 수정과 삭제는 소유자만 할 수 있다.
- 소유자가 아니면 `403 Forbidden`을 반환한다.
- 소유권 검증은 Controller가 아니라 컬렉션 엔티티(`Collection.validateOwner`)에서 수행한다.
  HTTP 진입점이 달라져도 같은 정책을 적용한다.

## 8. 오류 처리

작품 수, 중복 작품, 대표 작품 정책 위반은 모두 컬렉션 도메인 예외로 처리한다.
따라서 이 세 가지는 요청 DTO의 Bean Validation으로 검증하지 않는다. DTO는 `null` 여부와 문자열 길이만 검증한다.

| 코드 | 상황 | 상태 |
|---|---|---|
| `COLLECTION-001` | 해당 ID를 가진 컬렉션이 없음 | 404 |
| `COLLECTION-002` | 작품 수가 1개 미만이거나 100개 초과 | 400 |
| `COLLECTION-003` | 같은 작품을 중복으로 포함 | 400 |
| `COLLECTION-004` | 대표 작품이 포함 작품이 아님 | 400 |
| `COLLECTION-005` | 소유자가 아닌 사용자의 수정·삭제 요청 | 403 |
| `NOVEL-001` | 요청한 작품이 존재하지 않음 | 404 |

요청한 작품의 존재 검증은 컬렉션 전용 오류를 새로 만들지 않고 기존 작품 도메인 오류를 재사용한다.

## 9. 구현 구조

저장소 공통 규칙(`CLAUDE.md`)을 컬렉션 기능에도 그대로 적용한다.

### 9.1 쿼리는 QueryDSL로 직접 작성한다

- 커스텀 쿼리를 `@Query` 어노테이션이나 네이티브 쿼리로 표현하지 않는다.
- `CollectionCustomRepository` 인터페이스에 쿼리를 선언하고 `CollectionCustomRepositoryImpl`에서
  `JPAQueryFactory`와 생성된 Q 타입으로 구현한다.
- `CollectionRepository`는 `JpaRepository`와 `CollectionCustomRepository`를 함께 상속한다.
- 쓰기 잠금은 어노테이션이 아니라 QueryDSL의 `setLockMode(LockModeType.PESSIMISTIC_WRITE)`로 지정한다.
- 포함 작품을 함께 읽는 조회는 `fetchJoin()`으로 한 번에 가져와 작품 수만큼 추가 조회가 생기지 않게 한다.
  fetch join은 컬렉션 하나를 작품 수만큼의 행으로 돌려주므로 중복을 제거한 뒤 사용한다.

### 9.2 유스케이스 트랜잭션 경계는 Application이 소유하고 Service는 메서드 단위 속성을 명시한다

- 컬렉션 생성·수정·삭제 유스케이스 전체의 트랜잭션은 `CollectionManagementApplication`이 연다.
- 회원 탈퇴 유스케이스 전체의 트랜잭션은 `AccountApplication`이 연다.
- `CollectionService`의 Repository 접근 public 메서드는 각자 필요한 트랜잭션 속성을 명시한다.
  Service를 어디서 호출하든 그 메서드에 필요한 트랜잭션 속성이 코드에 드러나야 하기 때문이다.

| 메서드 | 속성 | 이유 |
|---|---|---|
| `create` | `@Transactional` | 저장 후 즉시 반영하는 데이터 변경 |
| `flushChanges` | `@Transactional` | 변경 감지 결과를 즉시 반영 |
| `delete` | `@Transactional` | 데이터 변경 |
| `updateOwnerToUnknown` | `@Transactional` | 탈퇴 시 소유자 벌크 갱신 |
| `getOwnedCollectionOrException` | `@Transactional` | 조회만 하지만 비관적 쓰기 잠금이 필요해 읽기 전용으로 둘 수 없다 |
| `getCollectionOrException` | `@Transactional(readOnly = true)` | 잠금 없는 단순 조회 |

- 전파 속성은 모두 기본값(`REQUIRED`)이다. Application이 이미 트랜잭션을 열었으면 새로 열지 않고 참여하므로,
  유스케이스 전체가 하나의 트랜잭션으로 묶인다는 사실은 그대로다.
- 따라서 쓰기 잠금, 지연 로딩, `flush`, 연쇄 삭제는 여전히 Application이 연 하나의 트랜잭션 안에서 동작한다.
- Service 단독 호출도 최소한 자기 메서드에 필요한 트랜잭션 안에서 동작한다.
  다만 여러 Service 호출을 하나의 원자적 단위로 묶는 책임은 여전히 Application에 있다.

## 10. 데이터 무결성

- 같은 컬렉션에 같은 작품이 여러 번 저장되지 않아야 한다.
- `(collection_id, novel_id)` 유니크 제약조건을 애플리케이션 엔티티와 실제 개발·운영 DB에 동일하게 적용한다.
- 컬렉션 작품 유니크 제약조건 이름은 `uk_collection_novel_collection_novel`을 사용한다.
- 생성과 수정은 저장 시점에 제약조건 위반을 확인할 수 있도록 즉시 반영한 뒤,
  해당 제약조건의 중복만 컬렉션 도메인 예외(`COLLECTION-003`)로 변환한다.
  지정한 제약조건 위반만 변환하고 그 외 데이터 무결성 오류는 숨기지 않는다.
  이는 동시에 같은 작품을 추가한 요청이 일반적인 DB 무결성 메시지로 노출되는 것을 막기 위한 방어다.
- "작품 1개 이상"은 DB 제약조건으로 표현할 수 없으므로 애플리케이션 불변식으로만 보장한다.
- "대표 작품은 포함 작품 중 하나"도 DB 제약조건으로 표현할 수 없으므로 애플리케이션 불변식으로만 보장한다.

## 11. 테이블과 제약조건

이 저장소에는 마이그레이션 도구가 없다. 따라서 신규 DDL은 엔티티의 `@Table`/`@UniqueConstraint`/`@Index` 선언과
이 문서로 관리하고, 실제 개발·운영 DB에는 아래 스크립트를 별도로 적용한다.

### 11.1 `collection`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `collection_id` | BIGINT | PK, AUTO_INCREMENT | 컬렉션 PK |
| `user_id` | BIGINT | NOT NULL, FK → `user` | 컬렉션을 만든 사용자 |
| `name` | VARCHAR(20) | NOT NULL | 컬렉션 이름 |
| `description` | VARCHAR(60) | NULL | 컬렉션 설명, 없으면 NULL |
| `is_public` | BOOLEAN | NOT NULL | 공개 여부, DB 기본값을 두지 않고 애플리케이션 생성자가 기본 공개(`true`)를 적용한다 |
| `representative_novel_id` | BIGINT | NOT NULL | 대표 작품, FK를 걸지 않고 애플리케이션이 포함 여부를 보장 |
| `created_date` | DATETIME(6) | NOT NULL | 컬렉션 최초 생성 시점, #561 목록 정렬 기준 |
| `modified_date` | DATETIME(6) | NOT NULL | 최종 수정 시점 |

인덱스: `idx_collection_user_created (user_id, created_date, collection_id)`
— #561의 사용자별 컬렉션 목록을 최초 생성 시점 기준으로 커서 페이지네이션하기 위한 인덱스다.

### 11.2 `collection_novel`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `collection_novel_id` | BIGINT | PK, AUTO_INCREMENT | 컬렉션 작품 PK |
| `collection_id` | BIGINT | NOT NULL, FK → `collection` | 작품이 포함된 컬렉션 |
| `novel_id` | BIGINT | NOT NULL, FK → `novel` | 컬렉션에 포함된 작품 |
| `created_date` | DATETIME(6) | NOT NULL | 작품이 컬렉션에 추가된 시점, 수정 시 보존한다 |
| `modified_date` | DATETIME(6) | NOT NULL | 최종 수정 시점 |

유니크 제약조건: `uk_collection_novel_collection_novel (collection_id, novel_id)`
인덱스: `idx_collection_novel_collection_created (collection_id, created_date, collection_novel_id)`
— #561의 추가 시점 기준 정렬과 최근 추가 작품 미리보기를 위한 인덱스다. 같은 요청으로 여러 작품을 동시에 추가하면
`created_date`가 같을 수 있으므로 `collection_novel_id`를 함께 정렬 기준으로 사용해 순서를 결정적으로 만든다.

### 11.3 참고 DDL

`user`와 `novel` 테이블의 실제 이름과 PK 이름은 적용 전에 확인한다.

외래 키에 `ON DELETE CASCADE`를 걸지 않는다. 회원 탈퇴 시 소유 컬렉션 정리는 6.1처럼 애플리케이션이 담당한다.

`is_public`에 DB `DEFAULT`를 두지 않는다. 3절에서 정한 대로 기본 공개는 애플리케이션 생성자가 적용하며,
값 없이 INSERT되는 경로가 없으므로 DB 기본값은 쓰이지 않는다.
이미 `DEFAULT TRUE`로 만든 DB가 있다면 아래로 기본값만 제거한다. 기존 행의 값은 바뀌지 않는다.

```sql
ALTER TABLE collection ALTER COLUMN is_public DROP DEFAULT;
```

```sql
CREATE TABLE collection (
    collection_id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                 BIGINT       NOT NULL,
    name                    VARCHAR(20)  NOT NULL,
    description             VARCHAR(60)  NULL,
    is_public               BOOLEAN      NOT NULL,
    representative_novel_id BIGINT       NOT NULL,
    created_date            DATETIME(6)  NOT NULL,
    modified_date           DATETIME(6)  NOT NULL,
    PRIMARY KEY (collection_id),
    KEY idx_collection_user_created (user_id, created_date, collection_id),
    CONSTRAINT fk_collection_user FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE TABLE collection_novel (
    collection_novel_id BIGINT      NOT NULL AUTO_INCREMENT,
    collection_id       BIGINT      NOT NULL,
    novel_id            BIGINT      NOT NULL,
    created_date        DATETIME(6) NOT NULL,
    modified_date       DATETIME(6) NOT NULL,
    PRIMARY KEY (collection_novel_id),
    UNIQUE KEY uk_collection_novel_collection_novel (collection_id, novel_id),
    KEY idx_collection_novel_collection_created (collection_id, created_date, collection_novel_id),
    CONSTRAINT fk_collection_novel_collection FOREIGN KEY (collection_id) REFERENCES collection (collection_id),
    CONSTRAINT fk_collection_novel_novel      FOREIGN KEY (novel_id)      REFERENCES novel (novel_id)
);
```

## 12. 테스트로 검증한 범위와 한계

이 저장소에는 H2나 Testcontainers 같은 DB 통합 테스트 인프라가 없고 `@DataJpaTest`를 사용하는 테스트도 없다.
따라서 단위 테스트로 검증할 수 있는 범위와 실제 DB에서만 확인할 수 있는 범위를 구분한다.

단위 테스트로 검증한 것

- 작품 수 경계값(0/1/2/99/100/101), 중복 작품, 대표 작품 포함 여부, 대표 작품 `null`
- 설명 정규화와 이름 앞뒤 공백 유지
- 생성 시 공개 여부 생략의 기본값 처리와 명시적 `false` 유지, 수정으로 공개 여부를 양방향으로 바꾸는 것
- 수정 DTO가 공개 여부의 `null`을 거부하는 것
- 수정 시 유지되는 작품의 `CollectionNovel` 인스턴스가 교체되지 않고 `createdDate`가 보존되는 것
- 제거된 작품만 빠지고 새 작품만 추가되는 delta 결과
- 소유자·비소유자 판정과 상태 코드
- 검증 순서(컬렉션 존재 → 소유자 → 작품 정책 → 작품 존재)
- 작품 존재 검증이 작품 수와 무관하게 조회 한 번으로 끝나는 것
- 요청 배열 순서 유지
- 응답 상태 코드와 생성 응답의 `collectionId`
- 요청 DTO Bean Validation 메시지
- 유니크 제약조건 위반 판별기의 이름 매칭과 테이블명 접두 정규화
- 포함 작품만 바뀌는 수정과 아무것도 바뀌지 않는 수정이 모두 `modified_date`를 갱신하는 것,
  실패한 수정은 갱신하지 않는 것
- 수정·삭제 대상 조회가 잠금 쿼리를, 조회 전용 경로가 잠금 없는 쿼리를 사용하는 것
- 잠금 쿼리가 `setLockMode(PESSIMISTIC_WRITE)`로 조립되고 작품을 조인하지 않는 것,
  조회 전용 쿼리는 잠금을 걸지 않고 `fetchJoin()`으로 자식을 함께 읽는 것
  (`JPAQueryFactory`를 대역으로 두고 조립된 쿼리를 확인한다)
- 탈퇴 정리 쿼리가 `collection.user_id`만 `-1`로 바꾸는 `update`로 조립되고 대상 조건이 탈퇴 사용자인 것,
  컬렉션을 조회하거나 삭제하지 않는 것
- 회원 탈퇴 시 소유자 이관이 사용자 삭제보다 먼저, 피드·댓글 작성자 익명화와 나란히 일어나는 것과,
  이관 실패 시 사용자를 지우지 않는 것

자동 테스트로 강제하지 않는 것

엔티티 매핑 메타데이터(테이블명, 유니크 제약조건, 인덱스, cascade와 orphanRemoval, 컬럼 길이와 nullable,
공개 여부의 원시 타입과 `columnDefinition` 부재)와 리포지토리·트랜잭션 구조 규약(`@Query` 미사용,
트랜잭션 속성 명시와 경계 위치)은 3·9·10·11절과 `CLAUDE.md` 코드 컨벤션이 정의하고 코드 리뷰로 지킨다.

실제 DB에서만 확인할 수 있는 것

- 위 DDL이 적용된 뒤 `uk_collection_novel_collection_novel`이 실제로 중복 저장을 막는지
- 컬렉션 삭제 시 `collection_novel`이 실제로 함께 삭제되는지
- 동시에 같은 작품을 추가하는 요청에서 제약조건 위반이 `COLLECTION-003`으로 변환되는지
- 인덱스가 #561 조회 쿼리에 실제로 사용되는지
- 쓰기 잠금이 실제로 동시 수정을 직렬화하는지와 잠금 대기·교착 상태가 없는지.
  단위 테스트는 쿼리에 `PESSIMISTIC_WRITE`가 지정된다는 사실까지만 확인할 수 있고,
  MySQL이 실제로 `SELECT ... FOR UPDATE`를 실행해 동시 요청을 막는지는 DB 없이는 증명할 수 없다
- `fetchJoin()`으로 자식을 함께 읽어 실제로 추가 조회(N+1)가 발생하지 않는지
- 회원 탈퇴가 FK 위반 없이 끝나는지와, 이관된 컬렉션이 실제로 `user_id = -1`로 남아 보존되는지
- 명시적으로 표시한 `modified_date`가 auditing에 덮어써지지 않고 그대로 저장되는지

## 13. 아직 확정되지 않은 정책

- 컬렉션 목록·상세 조회와 공개 범위, 차단 관계 접근 제어는 #561에서 정한다.
  `BLOCK_POLICY.md`는 현재 차단 정책이 컬렉션에 적용되어 있지 않다고 명시하고 있으므로 #561 완료 시 함께 갱신한다.
- 컬렉션 좋아요와 좋아요 종속 데이터 정리는 #562에서 정한다.
  좋아요를 구현하면 컬렉션 삭제(6절)에서 컬렉션보다 **먼저** 좋아요를 지워야 한다.
  회원 탈퇴(6.1절)는 컬렉션을 보존하므로, 탈퇴한 사용자가 남긴 좋아요만 어떻게 처리할지 그때 정한다.
- 작품이 삭제될 때 해당 작품을 포함한 컬렉션을 어떻게 처리할지는 정하지 않았다.
  현재는 `collection_novel.novel_id`에 FK가 있어 작품을 지우려면 컬렉션 작품을 먼저 정리해야 한다.
  이 경우 컬렉션이 작품 0개가 되거나 대표 작품을 잃을 수 있다. #559가 "삭제된 작품의 컬렉션 보존 정책"을
  제외 범위로 두었으므로 별도로 결정한다.
- 컬렉션 이름과 설명의 개행·이모지 처리는 기존 피드와 동일하게 별도 처리를 하지 않는다.

사용자 탈퇴 시 컬렉션 처리는 6.1절에서 확정했다. DB `ON DELETE CASCADE`가 아니라 애플리케이션이
소유자를 알 수 없는 사용자로 넘기고 컬렉션은 보존한다.
