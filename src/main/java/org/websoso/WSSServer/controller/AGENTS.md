<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# controller

## Purpose
공통 REST API 컨트롤러. 피드·유저·알림처럼 독립 모듈로 분리되지 않은 기능(앱 버전, 키워드, 서재, 소설, SosoPick 등)의 엔드포인트를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `AppVersionController.java` | 앱 최소 버전 조회 API |
| `KeywordController.java` | 키워드 검색 API |
| `LibraryController.java` | 유저 서재 조회·평가·관심 등록 API |
| `NovelController.java` | 소설 검색·상세 조회 API |
| `SosoPickController.java` | 오늘의 발견(SosoPick) 조회 API |

## For AI Agents

### Working In This Directory
- 컨트롤러는 요청 파싱과 응답 직렬화만 담당합니다. 비즈니스 로직은 `application/` 레이어에 위임합니다.
- `@CurrentUser Long userId`로 인증된 유저 ID를 주입받습니다.
- 피드·유저·알림·OAuth2 컨트롤러는 각 기능 모듈 패키지(`feed/controller/`, `user/controller/` 등)에 위치합니다.
- 응답은 `ResponseEntity<Xxx Response>`로 반환합니다.

### Common Patterns
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/...")
public class FooController {
    private final FooApplication fooApplication;

    @GetMapping
    public ResponseEntity<FooResponse> getFoo(@CurrentUser Long userId) {
        return ResponseEntity.ok(fooApplication.getFoo(userId));
    }
}
```

## Dependencies

### Internal
- `application/` — 유스케이스 호출
- `dto/` — 요청/응답 DTO
- `auth/` — `@CurrentUser` 파라미터 주입

<!-- MANUAL: -->
