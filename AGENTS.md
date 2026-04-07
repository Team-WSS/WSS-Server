<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# WSS-Server

## Purpose
Spring Boot 3.2.5 기반 멀티모듈 백엔드 API 서버 (Java 17). 소셜 독서 플랫폼 "소소(Soso)"의 서버로, 소설 검색·평가, 소소피드(SNS), 유저 서재 관리, 알림, OAuth2 로그인 등의 기능을 제공합니다.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle` | 루트 Gradle 빌드 설정 (의존성, 플러그인) |
| `settings.gradle` | 멀티모듈 설정 (wss-common, wss-service-support 포함) |
| `gradle.properties` | 버전 상수 모음 |
| `Dockerfile` | 컨테이너 빌드 정의 |
| `gradlew` / `gradlew.bat` | Gradle Wrapper 실행 스크립트 |
| `TEST_GUIDELINES.md` | 테스트 작성 가이드라인 |
| `refactor.md` | 리팩터링 진행 노트 |
| `user-novels-refactor-plan.md` | UserNovel 리팩터링 계획 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `src/` | 메인 API 애플리케이션 소스 코드 (see `src/AGENTS.md`) |
| `wss-common/` | 공통 엔티티·예외 추상화 공유 모듈 (see `wss-common/AGENTS.md`) |
| `wss-service-support/` | 로깅·버전 관리 등 지원 기능 공유 모듈 (see `wss-service-support/AGENTS.md`) |
| `config-repo/` | 런타임 환경설정 파일 (application-common.yml 등) |
| `.github/` | CI/CD 워크플로우 및 이슈/PR 템플릿 |
| `api document/` | API 명세서 (Korean, feature별 문서) |

## For AI Agents

### Working In This Directory
- 루트에서 직접 소스를 수정하지 않습니다. 소스는 `src/` 혹은 서브모듈 안에 있습니다.
- 새 기능은 반드시 기존 패키지 계층(`controller → application → service → repository → domain`)을 따릅니다.
- `config-repo/`에 실제 시크릿(API 키, private key)을 커밋하지 않습니다.

### Build & Test Commands
```bash
./gradlew clean build          # 전체 컴파일 + 테스트
./gradlew test                 # JUnit 5 테스트만 실행
./gradlew bootRun              # 로컬 서버 실행
./gradlew build -x test        # CI 스타일 빌드 (테스트 제외)
```

### Commit Conventions
커밋 접두어: `[FEAT]`, `[FIX]`, `[REFACTOR]`, `[CHORE]`, `[HOTFIX]`  
PR 병합 전 대상 브랜치(`dev` 또는 `main`)의 CI 통과 필수.

### Common Patterns
- 패키지명 소문자, 클래스 PascalCase, 메서드/필드 camelCase, 상수 UPPER_SNAKE_CASE
- DTO는 `dto/<feature>/` 하위에 `...Request` / `...Response` 접미사 사용
- QueryDSL Custom Repository: 인터페이스 + `Impl` 구현체 패턴

## Dependencies

### Internal Modules
- `wss-common` — 공통 BaseEntity, 공통 예외
- `wss-service-support` — 요청/SQL 로깅, 앱 버전 관리

### External
- Spring Boot 3.2.5, Spring Security, Spring Data JPA
- MySQL (운영 DB), Redis (캐싱)
- QueryDSL — 타입 안전 동적 쿼리
- JJWT 0.11.5 — JWT 토큰 처리
- Firebase Cloud Messaging — 푸시 알림
- AWS S3 — 이미지 파일 저장
- JUnit 5 + Spring Boot Test — 테스트

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
