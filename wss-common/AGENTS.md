<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# wss-common

## Purpose
메인 모듈과 `wss-service-support`가 공유하는 최소 공통 라이브러리. BaseEntity(생성/수정 시각 자동 관리)와 공통 예외 추상화를 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle` | 서브모듈 빌드 설정 |
| `src/main/java/org/websoso/common/entity/` | JPA BaseEntity 등 공통 엔티티 클래스 |
| `src/main/java/org/websoso/common/exception/` | 공통 예외 인터페이스/추상 클래스 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `src/main/java/org/websoso/common/entity/` | 공통 JPA 엔티티 (BaseEntity) |
| `src/main/java/org/websoso/common/exception/` | 공통 예외 계약 (인터페이스/추상 클래스) |

## For AI Agents

### Working In This Directory
- 이 모듈에 비즈니스 로직을 추가하지 않습니다. 순수 공통 추상화만 포함합니다.
- 변경 시 메인 모듈과 `wss-service-support` 모두 영향을 받으므로 하위 호환성에 주의합니다.
- `./gradlew :wss-common:build`로 독립 빌드 가능합니다.

### Testing Requirements
`src/test/java`에 단위 테스트 작성. 현재는 최소한의 테스트만 존재합니다.

## Dependencies

### External
- Spring Data JPA (BaseEntity용 `@EntityListeners`)

<!-- MANUAL: -->
