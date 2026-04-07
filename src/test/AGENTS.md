<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# src/test

## Purpose
JUnit 5 기반 테스트 코드 루트. 애플리케이션 레이어·피드·서재 기능의 서비스 단위 테스트를 포함합니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `java/org/websoso/WSSServer/` | 테스트 패키지 루트 |
| `java/org/websoso/WSSServer/application/` | Application 레이어 유스케이스 테스트 |
| `java/org/websoso/WSSServer/feed/` | 피드 서비스 테스트 |
| `java/org/websoso/WSSServer/library/` | 서재(Library) 서비스 테스트 |
| `java/org/websoso/WSSServer/util/` | 유틸리티 클래스 테스트 |

## For AI Agents

### Working In This Directory
- 테스트 클래스 네이밍: `*Test` (예: `FeedServiceTest`)
- 새 비즈니스 로직 추가 시 동일 패키지 경로에 단위 테스트를 함께 작성합니다.
- 버그 수정 시 재발 방지를 위한 회귀 테스트를 반드시 추가합니다.
- 통합 테스트보다 서비스 레이어 단위 테스트를 우선합니다.

### Testing Requirements
```bash
./gradlew test            # 전체 테스트 실행
./gradlew test --tests "org.websoso.WSSServer.feed.*"  # 특정 패키지만 실행
```

### Common Patterns
- `@SpringBootTest` + `@Transactional` 조합으로 서비스 통합 테스트
- Mock 사용 시 `@MockBean` 또는 Mockito `@Mock` 활용
- `TEST_GUIDELINES.md`(루트)에 상세 가이드 참고

<!-- MANUAL: -->
