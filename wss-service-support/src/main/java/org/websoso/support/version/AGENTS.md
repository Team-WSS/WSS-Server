<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# wss-service-support/version

## Purpose
앱 최소 버전 관리 기능 모듈. 클라이언트 앱의 강제 업데이트 여부를 판단하는 버전 정보를 DB에서 관리하며, 도메인·DTO·예외·리포지터리·서비스·유스케이스 레이어로 구성됩니다.

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `domain/` | 앱 버전 JPA 엔티티 |
| `dto/` | 버전 조회 요청/응답 DTO |
| `exception/` | 버전 관련 커스텀 예외·에러 |
| `repository/` | 버전 JPA 리포지터리 |
| `service/` | 버전 조회 서비스 |
| `usecase/` | 버전 조회 유스케이스 인터페이스 |

## For AI Agents

### Working In This Directory
- 메인 모듈의 `AppVersionApplication`이 이 모듈의 유스케이스를 호출합니다.
- `usecase/` 인터페이스를 통해 메인 모듈과 느슨하게 결합됩니다.
- 테스트는 `src/test/java/org/websoso/support/version/`에 작성합니다.

### Testing Requirements
```bash
./gradlew :wss-service-support:test
```

<!-- MANUAL: -->
