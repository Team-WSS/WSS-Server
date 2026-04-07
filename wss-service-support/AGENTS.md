<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# wss-service-support

## Purpose
메인 모듈에서 재사용하는 지원 기능 공유 모듈. HTTP 요청 로깅, SQL 로깅, 앱 최소 버전 관리 기능을 독립 모듈로 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle` | 서브모듈 빌드 설정 |
| `src/main/resources/META-INF/spring/` | Spring Boot 자동 설정 등록 파일 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `src/main/java/org/websoso/support/logging/` | HTTP 요청 및 SQL 쿼리 로깅 지원 |
| `src/main/java/org/websoso/support/logging/request/` | 요청 로깅 필터/인터셉터 |
| `src/main/java/org/websoso/support/logging/sql/` | SQL 실행 로깅 |
| `src/main/java/org/websoso/support/version/` | 앱 최소 버전 관리 도메인·서비스·유스케이스 |
| `src/test/java/org/websoso/support/version/` | 버전 관리 테스트 |

## For AI Agents

### Working In This Directory
- 메인 모듈(`WSSServer`)에서 `implementation project(':wss-service-support')`로 의존합니다.
- 새 지원 기능 추가 시 `META-INF/spring/`에 자동 설정을 등록하여 메인 모듈에서 별도 설정 없이 동작하게 합니다.
- `./gradlew :wss-service-support:test`로 독립 테스트 실행 가능합니다.

### Testing Requirements
`src/test/java/org/websoso/support/version/`에 기존 버전 관련 테스트 참고.

## Dependencies

### Internal
- `wss-common` — BaseEntity, 공통 예외

### External
- Spring Boot Web, Spring Data JPA

<!-- MANUAL: -->
