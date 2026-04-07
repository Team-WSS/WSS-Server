<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# dto

## Purpose
기능별 요청(Request)·응답(Response) DTO 패키지. 컨트롤러 ↔ 애플리케이션 레이어 간 데이터 전달 객체를 기능별 서브패키지로 분리 관리합니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `auth/` | 토큰 재발급·FCM 토큰 저장 DTO |
| `avatar/` | 아바타 프로필 목록 조회 DTO |
| `block/` | 유저 차단 DTO |
| `comment/` | 댓글 작성·조회 DTO |
| `feed/` | 피드 작성·조회·수정 DTO |
| `keyword/` | 키워드 검색 DTO |
| `novel/` | 소설 검색·상세 조회 DTO |
| `platform/` | 플랫폼(Apple, Kakao) 관련 DTO |
| `popularFeed/` | 인기 피드 조회 DTO |
| `popularNovel/` | 인기 소설 조회 DTO |
| `sosoPick/` | 오늘의 발견 DTO |
| `user/` | 유저 프로필·취향·정보 DTO |
| `userNovel/` | 유저 서재(UserNovel) DTO |
| `version/` | 앱 버전 DTO |

## For AI Agents

### Working In This Directory
- DTO 클래스명 규칙: `...Request` (요청), `...Response` (응답), `...GetResponse` (조회 응답).
- DTO는 순수 데이터 컨테이너입니다. 비즈니스 로직을 포함하지 않습니다.
- `record` 또는 `@Getter + @Builder` 패턴을 사용합니다.
- 새 기능 추가 시 기능명에 맞는 서브패키지를 생성합니다.

### Common Patterns
```java
// 응답 DTO 예시
public record FooGetResponse(Long id, String name) {
    public static FooGetResponse of(Foo foo) {
        return new FooGetResponse(foo.getId(), foo.getName());
    }
}
```

<!-- MANUAL: -->
