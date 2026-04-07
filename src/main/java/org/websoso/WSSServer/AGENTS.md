<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-04-03 | Updated: 2026-04-03 -->

# org.websoso.WSSServer

## Purpose
메인 API 애플리케이션의 루트 패키지. Spring Boot 진입점과 모든 기능 패키지를 포함합니다. 계층형 아키텍처(`controller → application → service → repository → domain`)를 따릅니다.

## Key Files

| File | Description |
|------|-------------|
| `WssServerApplication.java` | `@SpringBootApplication` 진입점 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `application/` | 유스케이스 오케스트레이션 (비즈니스 흐름 조율) (see `application/AGENTS.md`) |
| `auth/` | JWT 인증·인가 필터, 인가 검증기 (see `auth/AGENTS.md`) |
| `config/` | Spring 설정 클래스 (Security, QueryDSL, S3, JWT 등) (see `config/AGENTS.md`) |
| `controller/` | REST API 엔드포인트 컨트롤러 (see `controller/AGENTS.md`) |
| `domain/` | JPA 엔티티 및 도메인 열거형 (see `domain/AGENTS.md`) |
| `dto/` | 기능별 요청/응답 DTO (see `dto/AGENTS.md`) |
| `exception/` | 도메인별 커스텀 예외·에러 코드·전역 핸들러 (see `exception/AGENTS.md`) |
| `feed/` | 소소피드 기능 모듈 (controller·domain·repository·service) (see `feed/AGENTS.md`) |
| `library/` | 유저 서재 기능 모듈 (see `library/AGENTS.md`) |
| `notification/` | 푸시 알림 기능 모듈 (FCM 포함) (see `notification/AGENTS.md`) |
| `novel/` | 소설 검색·조회 기능 모듈 (see `novel/AGENTS.md`) |
| `oauth2/` | Apple·Kakao OAuth2 소셜 로그인 (see `oauth2/AGENTS.md`) |
| `repository/` | 공통 JPA 리포지터리 (Genre 등) |
| `service/` | 공통 서비스 (ImageClient, SosoPickService) |
| `user/` | 유저 계정·프로필·차단 기능 모듈 (see `user/AGENTS.md`) |
| `util/` | 유틸리티 클래스 (시간 포맷, 컨버터 등) |
| `validation/` | 커스텀 Bean Validation 어노테이션 및 Validator |

## For AI Agents

### Working In This Directory
- 새 기능은 반드시 기존 계층 구조를 따릅니다: `controller → application → service → repository → domain`
- 기능이 크면 `feed/`, `user/`처럼 독립 모듈 패키지(controller·domain·repository·service 하위)로 구성합니다.
- 소규모 공통 기능은 `service/`, `repository/` 공통 패키지에 배치합니다.

### Common Patterns
- Application 클래스: 여러 서비스를 조합하는 유스케이스 오케스트레이터
- Custom Repository: `FooCustomRepository` 인터페이스 + `FooCustomRepositoryImpl` (QueryDSL)
- 예외: `Custom<Domain>Exception` + `Custom<Domain>Error` enum 쌍으로 구성

## Dependencies

### Internal Modules
- `wss-common` — BaseEntity, 공통 예외
- `wss-service-support` — 로깅, 버전 관리

<!-- MANUAL: -->
